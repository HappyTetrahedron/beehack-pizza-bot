package io.beekeeper.bots.pizza;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.beekeeper.bots.pizza.shell.ProcessExecutor;

public class OrderHelper {

    public static Promise<Void, Void, Void> executeOrder(Collection<OrderItem> orderItems, boolean dryRun) {
        DeferredObject<Void, Void, Void> deferred = new DeferredObject<>();

        List<String> command = new ArrayList<>(Arrays.asList(
                "node",
                "pizza-ordering/app.js",
                toJSON(orderItems).toString()
        ));
        if (!dryRun) {
            command.add("-x");
        }

        new Thread(() -> {
            try {
                System.out.println("command = " + command);
                ProcessExecutor.CommandResult result = ProcessExecutor.executeCommand(command);
                if (result.getExitCode() == 0) {
                    deferred.resolve(null);
                } else {
                    deferred.reject(null);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        return deferred.promise();
    }

    private static JsonArray toJSON(Collection<OrderItem> orderItems) {
        JsonArray jsonArray = new JsonArray();
        orderItems.forEach(item -> jsonArray.add(toJSON(item)));
        return jsonArray;
    }

    private static JsonObject toJSON(OrderItem item) {
        String articleNumber = item.getMenuItem().getArticleNumber();
        if (item.getMenuItem().getParentArticleNumber() != null) {
            articleNumber = item.getMenuItem().getParentArticleNumber();
        }

        JsonObject json = new JsonObject();
        json.addProperty("articleId", item.getMenuItem().getArticleId());
        json.addProperty("articleNumber", articleNumber);
        json.addProperty("commodityGroupId", item.getMenuItem().getCommodityGroupId());
        return json;
    }
}
