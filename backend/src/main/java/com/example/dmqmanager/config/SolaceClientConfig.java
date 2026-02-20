package com.example.dmqmanager.config;

import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolaceClientConfig {

    @Bean(destroyMethod = "closeSession")
    JCSMPSession jcsmpSession(AppProperties appProperties) throws Exception {
        var solace = appProperties.solace();
        JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, solace.host());
        properties.setProperty(JCSMPProperties.VPN_NAME, solace.vpn());
        properties.setProperty(JCSMPProperties.USERNAME, solace.clientUsername());
        properties.setProperty(JCSMPProperties.PASSWORD, solace.clientPassword());
        JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();
        return session;
    }
}
