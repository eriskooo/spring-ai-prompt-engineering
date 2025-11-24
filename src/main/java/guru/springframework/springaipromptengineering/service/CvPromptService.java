package guru.springframework.springaipromptengineering.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class CvPromptService {

    private final ChatModel chatModel;

    public CvPromptService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    // Reads resources/cv/cv.txt and calls the model with provided instruction
    public String callWithFirstCv(String instruction) {
        String cvText = readCvText("cv.txt");
        if (cvText == null || cvText.trim().isEmpty()) {
            throw new IllegalStateException("The CV text appears to be empty or unreadable: classpath:/cv/cv.txt");
        }
        return callModel(instruction, cvText);
    }

    // Convenience method with fixed Czech instruction
    public String findThreeCzechRemoteContractorPositionsFromFirstCv() {
        String instruction = "Na základě přiloženého životopisu najdi přesně 3 konkrétní pracovní pozice v České republice na IČO (OSVČ), plně remote. U každé pozice uveď název role dle inzerátu, jméno/značku zaměstnavatele nebo agentury, 1–2 věty proč je vhodná s odkazem na zkušenosti z CV, a klíčové technologie/kompetence. Přidej přesně 1 ověřený funkční odkaz (URL) přímo na detail daného inzerátu (ne seznam, ne vyhledávání, ne homepage). Pokud nelze doložit konkrétní inzerát, uveď NENALEZENO a krátké vysvětlení. Výstup naformátuj v Markdownu jako očíslovaný seznam 1..3, se sekcí Odkaz: s jediným URL a mini‑checklistem (Zaměstnavatel, Role, Remote: ano, OSVČ: ano).";
        return callWithFirstCv(instruction);
    }

    // Optional: read arbitrary text file under cv/
    public String callWithCv(String instruction, String fileName) {
        String cvText = readCvText(fileName);
        if (cvText == null || cvText.trim().isEmpty()) {
            throw new IllegalStateException("The CV text appears to be empty or unreadable: classpath:/cv/" + fileName);
        }
        return callModel(instruction, cvText);
    }

    private String readCvText(String fileName) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("classpath:cv/" + fileName);
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("CV text file not found or unreadable under classpath:/cv: " + fileName);
            }
            try (InputStream is = resource.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8).trim();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read CV text from resource: classpath:/cv/" + fileName, e);
        }
    }

    private String callModel(String instruction, String cvText) {
//        String template = String.join(System.lineSeparator(),
//                "[SYSTEM]",
//                "Jsi kariérní poradce a sourcér pracovních pozic. Postupuj přesně podle instrukce.",
//                "Pracuj pouze s obsahem životopisu mezi značkami <CV> a </CV>.",
//                "Neptej se na další údaje; pokud něco chybí, rozumně odhadni z kontextu.",
//                "Najdi a doporučuj pouze konkrétní pracovní inzeráty relevantní k profilu. Každý návrh musí obsahovat: zaměstnavatele/agenturu, název role dle inzerátu a přesně 1 přímý odkaz (URL) na DETAIL inzerátu.",
//                "Zakázané: víc odkazů, vyhledávací dotazy, seznamy nabídek, homepage a obecné rozcestníky. Preferuj české portály (.cz) a stránky kariér zaměstnavatelů.",
//                "Pokud nemůžeš doložit konkrétní ověřitelný inzerát, napiš NENALEZENO a stručně vysvětli proč.",
//                "Výstup formátuj v Markdownu a zachovej přehlednost (název pozice, zaměstnavatel, důvod, technologie, Odkaz + mini‑checklist).",
//                "Odpověz česky.",
//                "",
//                "[INSTRUKCE]",
//                "{instruction}",
//                "",
//                "[ŽIVOTOPIS]",
//                "<CV>",
//                "{cvText}",
//                "</CV>");


        String template = String.join(System.lineSeparator(),

                "Jsi programator hledajici praci.",
                "Na základě přiloženého životopisu hledej přesně 3 inzeraty v České republice na IČO (OSVČ), plně remote. uved url inzerátu .",
                "Obsah životopisu se nachazi mezi znackami<CV> a </CV>.",
                "Odpověz česky.",
                "",
                "[ŽIVOTOPIS]",
                "<CV>",
                "{cvText}",
                "</CV>");

        PromptTemplate promptTemplate = new PromptTemplate(template);
        promptTemplate.add("instruction", instruction);
        promptTemplate.add("cvText", cvText);
        Prompt prompt = promptTemplate.create();
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    private static String truncate(String input, int maxLen) {
        if (input == null) return "";
        if (input.length() <= maxLen) return input;
        return input.substring(0, Math.max(0, maxLen - 20)) + "... [truncated]";
    }

    private static String defaultIfBlank(String s, String def) {
        if (s == null) return def;
        if (s.trim().isEmpty()) return def;
        return s;
    }
}
