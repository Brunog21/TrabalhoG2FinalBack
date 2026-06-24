package br.edu.atitus.currencyservice.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BCBResponse - Testes Unitários (modelo de dados BCB)")
class BCBResponseTest {

    @Test
    @DisplayName("Deve criar BCBResponse, setar valor e recuperá-lo corretamente")
    void bcbResponse_SetterGetter_DeveFuncionar() {
        System.out.println("[DEBUG] Testando BCBResponse setters/getters...");

        BCBResponse.BCBValue bcbValue = new BCBResponse.BCBValue();
        bcbValue.setCotacaoVenda(5.75);

        BCBResponse response = new BCBResponse();
        response.setValue(List.of(bcbValue));

        assertNotNull(response.getValue(), "getValue() não deve ser nulo");
        assertEquals(1, response.getValue().size(), "Deve ter exatamente 1 item");
        assertEquals(5.75, response.getValue().get(0).getCotacaoVenda(), 0.001,
                "cotacaoVenda deve ser 5.75");

        System.out.println("[DEBUG] ✓ BCBResponse funcionando: cotacaoVenda="
                + response.getValue().get(0).getCotacaoVenda());
    }

    @Test
    @DisplayName("BCBResponse sem setValue deve retornar null em getValue")
    void bcbResponse_SemSetValue_DeveRetornarNull() {
        System.out.println("[DEBUG] Testando BCBResponse sem valor...");

        BCBResponse response = new BCBResponse();

        assertNull(response.getValue(),
                "getValue() deve ser null quando setValue() nunca foi chamado");

        System.out.println("[DEBUG] ✓ getValue() null quando não inicializado");
    }

    @Test
    @DisplayName("Deve aceitar lista com múltiplos BCBValue")
    void bcbResponse_MultiplosBCBValues_DeveFuncionar() {
        System.out.println("[DEBUG] Testando BCBResponse com múltiplos valores...");

        BCBResponse.BCBValue v1 = new BCBResponse.BCBValue();
        v1.setCotacaoVenda(5.50);

        BCBResponse.BCBValue v2 = new BCBResponse.BCBValue();
        v2.setCotacaoVenda(6.20);

        BCBResponse.BCBValue v3 = new BCBResponse.BCBValue();
        v3.setCotacaoVenda(7.00);

        BCBResponse response = new BCBResponse();
        response.setValue(List.of(v1, v2, v3));

        assertEquals(3, response.getValue().size(), "Deve ter 3 valores");
        assertEquals(5.50, response.getValue().get(0).getCotacaoVenda(), 0.001);
        assertEquals(6.20, response.getValue().get(1).getCotacaoVenda(), 0.001);
        assertEquals(7.00, response.getValue().get(2).getCotacaoVenda(), 0.001);

        System.out.println("[DEBUG] ✓ 3 BCBValues recuperados corretamente");
    }

    @Test
    @DisplayName("Deve aceitar lista vazia em setValue")
    void bcbResponse_ListaVazia_DeveRetornarListaVazia() {
        System.out.println("[DEBUG] Testando BCBResponse com lista vazia...");

        BCBResponse response = new BCBResponse();
        response.setValue(new ArrayList<>());

        assertNotNull(response.getValue(), "Lista não deve ser null");
        assertEquals(0, response.getValue().size(), "Lista deve estar vazia");
        assertTrue(response.getValue().isEmpty(), "isEmpty() deve ser true");

        System.out.println("[DEBUG] ✓ Lista vazia aceita corretamente");
    }

    @Test
    @DisplayName("Deve sobrescrever valor anterior ao chamar setValue novamente")
    void bcbResponse_SetValueDuasVezes_DeveSubstituirValorAnterior() {
        System.out.println("[DEBUG] Testando substituição de valor em BCBResponse...");

        BCBResponse.BCBValue primeiroValor = new BCBResponse.BCBValue();
        primeiroValor.setCotacaoVenda(5.00);

        BCBResponse.BCBValue segundoValor = new BCBResponse.BCBValue();
        segundoValor.setCotacaoVenda(6.00);

        BCBResponse response = new BCBResponse();
        response.setValue(List.of(primeiroValor));
        assertEquals(5.00, response.getValue().get(0).getCotacaoVenda(), 0.001);

        response.setValue(List.of(segundoValor)); // substitui
        assertEquals(6.00, response.getValue().get(0).getCotacaoVenda(), 0.001,
                "Segundo setValue deve substituir o primeiro");

        System.out.println("[DEBUG] ✓ setValue substitui valor anterior corretamente");
    }

    @Test
    @DisplayName("BCBValue deve aceitar cotação positiva comum")
    void bcbValue_CotacaoPositiva_DeveFuncionar() {
        System.out.println("[DEBUG] Testando BCBValue com cotação positiva...");

        BCBResponse.BCBValue value = new BCBResponse.BCBValue();
        value.setCotacaoVenda(5.75);

        assertEquals(5.75, value.getCotacaoVenda(), 0.001,
                "getCotacaoVenda deve retornar 5.75");

        System.out.println("[DEBUG] ✓ BCBValue cotação positiva: " + value.getCotacaoVenda());
    }

    @Test
    @DisplayName("BCBValue deve aceitar cotação zero")
    void bcbValue_CotacaoZero_DeveSerAceito() {
        System.out.println("[DEBUG] Testando BCBValue com cotação zero (edge case)...");

        BCBResponse.BCBValue value = new BCBResponse.BCBValue();
        value.setCotacaoVenda(0.0);

        assertEquals(0.0, value.getCotacaoVenda(), 0.001,
                "Cotação zero deve ser aceita");

        System.out.println("[DEBUG] ✓ BCBValue aceita cotação zero");
    }

    @Test
    @DisplayName("BCBValue deve aceitar cotação negativa (edge case)")
    void bcbValue_CotacaoNegativa_DeveSerAceito() {
        System.out.println("[DEBUG] Testando BCBValue com cotação negativa (edge case)...");

        BCBResponse.BCBValue value = new BCBResponse.BCBValue();
        value.setCotacaoVenda(-1.5);

        assertEquals(-1.5, value.getCotacaoVenda(), 0.001,
                "Cotação negativa deve ser aceita pelo modelo (validação é responsabilidade do serviço)");

        System.out.println("[DEBUG] ✓ BCBValue aceita cotação negativa: " + value.getCotacaoVenda());
    }

    @Test
    @DisplayName("BCBValue sem setCotacaoVenda deve retornar null")
    void bcbValue_SemSetCotacao_DeveRetornarNull() {
        System.out.println("[DEBUG] Testando BCBValue sem setCotacaoVenda...");

        BCBResponse.BCBValue value = new BCBResponse.BCBValue();

        assertNull(value.getCotacaoVenda(),
                "getCotacaoVenda() deve ser null antes de setCotacaoVenda()");

        System.out.println("[DEBUG] ✓ getCotacaoVenda() retorna null quando não inicializado");
    }

    @Test
    @DisplayName("BCBValue deve aceitar alta precisão decimal")
    void bcbValue_AltaPrecisaoDecimal_DeveFuncionar() {
        System.out.println("[DEBUG] Testando BCBValue com alta precisão decimal...");

        BCBResponse.BCBValue value = new BCBResponse.BCBValue();
        value.setCotacaoVenda(5.746521);

        assertEquals(5.746521, value.getCotacaoVenda(), 0.0001,
                "Deve preservar precisão decimal");

        System.out.println("[DEBUG] ✓ Alta precisão preservada: " + value.getCotacaoVenda());
    }
}