package guru.springframework.springaipromptengineering;

import guru.springframework.springaipromptengineering.service.CvPromptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class CvPromptServiceTests {

    @Autowired
    private CvPromptService cvPromptService;

    @Test
    void printsThreePositionsForFirstCv() {
        String response = cvPromptService.findThreeCzechRemoteContractorPositionsFromFirstCv();
        System.out.println("[TEST OUTPUT] Response for 3 CZ OSVC remote positions:\n" + response);
        assertNotNull(response);
        assertFalse(response.trim().isEmpty());
    }
}
