package io.beekeeper.bots.pizza;

import io.beekeeper.bots.pizza.crawler.DieciMenuItem;
import io.beekeeper.sdk.ChatBot;
import io.beekeeper.sdk.exception.BeekeeperException;
import io.beekeeper.sdk.model.Conversation;
import io.beekeeper.sdk.model.ConversationMessage;
import io.beekeeper.sdk.model.MessageType;
import io.beekeeper.sdk.params.SendMessageParams;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class PizzaBotTest {
    @Test
    public void test() throws BeekeeperException {
        Map<String, DieciMenuItem> menu = new HashMap<>();
        menu.put("Gorgonzola Pizza", new DieciMenuItem("Gorgonzola Whee"));
        menu.put("Margherita",  new DieciMenuItem("Margherita"));
        Parser<DieciMenuItem> parser = new Parser<>(menu);
        PizzaBot unit = new TestPizzaBot("https://pizza.dev.beekeeper.io", "token");
        unit.setParser(parser);

        TestConversationHelper conversationHelper = new TestConversationHelper();
        Conversation conversation = Conversation.builder().build();
        unit.processGroupMessage(conversation, conversationMessage("/start"), conversationHelper);
        unit.processGroupMessage(conversation, conversationMessage("/order margherita"), conversationHelper);

        assertTrue(unit.getOrderSession().getOrderItems().size() == 1);

    }

    private ConversationMessage conversationMessage(String text) {
        return ConversationMessage.builder()
                .conversationId(900434)
                .text(text)
                .type(MessageType.REGULAR)
                .build();
    }

    static class TestPizzaBot extends PizzaBot {
        public TestPizzaBot(String tenantUrl, String apiToken) {
            super(tenantUrl, apiToken);
        }
        @Override
        protected void sendPrivateConfirmationMessageToUser(Conversation conversation, ConversationMessage message, String itemName) throws BeekeeperException {
            return;
        }
    }

    static class TestConversationHelper implements ChatBot.ConversationHelper {
        @Override
        public void reply(String text) throws BeekeeperException {

        }

        @Override
        public void reply(SendMessageParams messageParams) throws BeekeeperException {

        }
    }
}
