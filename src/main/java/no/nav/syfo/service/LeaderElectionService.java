package no.nav.syfo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.LeaderPod;
import no.nav.syfo.metric.Metrikk;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;

@Service
@Slf4j
public class LeaderElectionService {

    private final Metrikk metrikk;
    private final RestTemplate restTemplateKubernetes;
    private final String electorpath;

    @Inject
    public LeaderElectionService(
            Metrikk metrikk,
            @Qualifier("kubernetes") RestTemplate restTemplateKubernetes,
            @Value("${elector.path}") String electorpath
    ) {
        this.metrikk = metrikk;
        this.restTemplateKubernetes = restTemplateKubernetes;
        this.electorpath = electorpath;
    }

    public boolean isLeader() {
        metrikk.tellHendelse("isLeader_kalt");
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "http://" + electorpath;

        String response = restTemplateKubernetes.getForObject(url, String.class);

        try {
            LeaderPod leader = objectMapper.readValue(response, LeaderPod.class);
            return isHostLeader(leader);
        } catch (IOException e) {
            log.error("Couldn't map response from electorPath to LeaderPod object", e);
            metrikk.tellHendelse("isLeader_feilet");
            throw new RuntimeException("Couldn't map response from electorpath to LeaderPod object", e);
        } catch (Exception e) {
            log.error("Something went wrong when trying to check leader", e);
            metrikk.tellHendelse("isLeader_feilet");
            throw new RuntimeException("Got exception when trying to find leader", e);
        }
    }

    private boolean isHostLeader(LeaderPod leader) throws Exception {
        String hostName = InetAddress.getLocalHost().getHostName();
        String leaderName = leader.getName();

        if (hostName.equals(leaderName)) {
            log.info("Host with name {} is leader", hostName);
            return true;
        }
        log.info("Host with name {} is not leader {}", hostName, leaderName);
        return false;
    }
}
