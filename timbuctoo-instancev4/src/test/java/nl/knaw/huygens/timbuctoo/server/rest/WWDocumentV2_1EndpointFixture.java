package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.concordion.extensions.HttpCommandExtension;
import nl.knaw.huygens.concordion.extensions.HttpResult;
import nl.knaw.huygens.concordion.extensions.ReplaceEmbeddedStylesheetExtension;
import org.concordion.api.extension.Extension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RunWith(ConcordionRunner.class)
public class WWDocumentV2_1EndpointFixture extends AbstractV2_1EndpointFixture {
    private final ObjectMapper objectMapper;

    @Extension
    public HttpCommandExtension commandExtension = new HttpCommandExtension(this::doHttpCommand, false);

    @Extension
    public ReplaceEmbeddedStylesheetExtension removeExtension = new ReplaceEmbeddedStylesheetExtension("/nl/knaw/huygens/timbuctoo/server/rest/concordion.css");

    public WWDocumentV2_1EndpointFixture() { this.objectMapper = new ObjectMapper(); }

    public int getNumberOfItems(HttpResult result) {
        JsonNode jsonNode = getBody(result);
        return Lists.newArrayList(jsonNode.elements()).size();
    }

    private JsonNode getBody(HttpResult result) {
        try {
            return objectMapper.readTree(result.getBody().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean doesNotContainResult(HttpResult resultToTest, HttpResult resultToBeContained) {
        List<String> idsToTest = getIds(resultToTest);
        List<String> idsToBeContained = getIds(resultToBeContained);

        return !idsToTest.containsAll(idsToBeContained);
    }

    private List<String> getIds(HttpResult result) {
        JsonNode body = getBody(result);
        ArrayList<String> ids = Lists.newArrayList();
        for (Iterator<JsonNode> elements = body.elements(); elements.hasNext(); ) {
            ids.add(elements.next().get("_id").textValue());
        }

        return ids;
    }
}
