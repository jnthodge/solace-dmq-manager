package com.example.dmqmanager.service;

import com.example.dmqmanager.config.AppProperties;
import com.example.dmqmanager.model.DmqMessage;
import com.example.dmqmanager.model.DmqSummary;
import com.example.dmqmanager.model.ReplayResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.solacesystems.jcsmp.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Service
public class DmqService {
    private final JCSMPSession session;
    private final WebClient webClient;
    private final AppProperties properties;

    public DmqService(JCSMPSession session, AppProperties properties) {
        this.session = session;
        this.properties = properties;
        var solace = properties.solace();
        String authValue = Base64.getEncoder().encodeToString((solace.sempUsername() + ":" + solace.sempPassword()).getBytes(StandardCharsets.UTF_8));
        this.webClient = WebClient.builder()
                .baseUrl(solace.sempUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + authValue)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<DmqSummary> listDmqs() {
        JsonNode data = webClient.get()
                .uri("/SEMP/v2/monitor/msgVpns/{vpn}/queues?count=500", properties.solace().vpn())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        List<DmqSummary> out = new ArrayList<>();
        if (data == null || data.path("data").isMissingNode()) {
            return out;
        }

        for (JsonNode queueNode : data.path("data")) {
            String queueName = queueNode.path("queueName").asText();
            if (!queueName.endsWith(".dmq")) {
                continue;
            }
            String originQueue = queueName.substring(0, queueName.length() - 4);
            long messageCount = queueNode.path("msgs").asLong(0);
            out.add(new DmqSummary(queueName, originQueue, messageCount));
        }
        out.sort(Comparator.comparing(DmqSummary::queueName));
        return out;
    }

    public List<DmqMessage> browseMessages(String queueName, int maxMessages) throws JCSMPException {
        Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        Browser browser = session.createBrowser(queue);
        List<DmqMessage> results = new ArrayList<>();
        BytesXMLMessage message;
        long idx = 0;
        while ((message = browser.getNext()) != null && idx < maxMessages) {
            idx++;
            results.add(new DmqMessage(
                    idx,
                    message.getMessageId(),
                    extractPayload(message),
                    message.getDestination() == null ? "" : message.getDestination().getName(),
                    Instant.ofEpochMilli(message.getSenderTimestamp()).toString()
            ));
        }
        browser.close();
        return results;
    }

    public ReplayResult replayQueueToOrigin(String queueName, boolean deleteFromDmqAfterCopy) throws JCSMPException {
        String originQueue = queueName.substring(0, queueName.length() - 4);
        Queue dmqQueue = JCSMPFactory.onlyInstance().createQueue(queueName);
        Queue destination = JCSMPFactory.onlyInstance().createQueue(originQueue);

        XMLMessageProducer producer = session.getMessageProducer(null);
        Browser browser = session.createBrowser(dmqQueue);
        long copied = 0;
        BytesXMLMessage browsed;
        while ((browsed = browser.getNext()) != null) {
            BytesMessage newMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
            newMsg.setData(extractBytes(browsed));
            producer.send(newMsg, destination);
            copied++;
        }
        browser.close();

        if (deleteFromDmqAfterCopy && copied > 0) {
            ConsumerFlowProperties flowProperties = new ConsumerFlowProperties();
            flowProperties.setEndpoint(dmqQueue);
            flowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
            FlowReceiver flowReceiver = session.createFlow(null, flowProperties);
            flowReceiver.start();
            for (long i = 0; i < copied; i++) {
                BytesXMLMessage msg = flowReceiver.receive(2000);
                if (msg == null) {
                    break;
                }
                msg.ackMessage();
            }
            flowReceiver.close();
        }
        producer.close();

        return new ReplayResult(queueName, originQueue, copied, deleteFromDmqAfterCopy);
    }

    private byte[] extractBytes(BytesXMLMessage message) {
        if (message instanceof BytesMessage bytesMessage) {
            return bytesMessage.getData();
        }
        if (message instanceof TextMessage textMessage) {
            return textMessage.getText() == null ? new byte[0] : textMessage.getText().getBytes(StandardCharsets.UTF_8);
        }
        return new byte[0];
    }

    private String extractPayload(BytesXMLMessage message) {
        if (message instanceof TextMessage textMessage) {
            return textMessage.getText();
        }
        if (message instanceof BytesMessage bytesMessage && bytesMessage.getData() != null) {
            return new String(bytesMessage.getData(), StandardCharsets.UTF_8);
        }
        return "<binary or unsupported payload>";
    }
}
