package com.example.dmqmanager.controller;

import com.example.dmqmanager.model.DmqMessage;
import com.example.dmqmanager.model.DmqSummary;
import com.example.dmqmanager.model.ReplayRequest;
import com.example.dmqmanager.model.ReplayResult;
import com.example.dmqmanager.service.DmqService;
import com.solacesystems.jcsmp.JCSMPException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dmqs")
public class DmqController {
    private final DmqService dmqService;

    public DmqController(DmqService dmqService) {
        this.dmqService = dmqService;
    }

    @GetMapping
    public List<DmqSummary> listDmqs() {
        return dmqService.listDmqs();
    }

    @GetMapping("/{queueName}/messages")
    public List<DmqMessage> listMessages(@PathVariable String queueName,
                                         @RequestParam(defaultValue = "200") int limit) throws JCSMPException {
        return dmqService.browseMessages(queueName, limit);
    }

    @PostMapping("/{queueName}/replay")
    public ReplayResult replay(@PathVariable String queueName,
                               @Valid @RequestBody ReplayRequest replayRequest) throws JCSMPException {
        return dmqService.replayQueueToOrigin(queueName, replayRequest.deleteFromDmqAfterCopy());
    }
}
