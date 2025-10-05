package dev.puzzleshq.CRArchiveBot.utils.threads;

import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.puzzleshq.CRArchiveBot.DiscordBot.*;

public class LoadingMessage {
    public static CompletableFuture<Void> runStepsSequentially(List<String> steps, List<Boolean> completed, Message message, int stepIndex) {
        if (stepIndex >= steps.size()) return CompletableFuture.completedFuture(null);

        ScheduledExecutorService spinnerExec = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger spinnerIndex = new AtomicInteger(0);

        Runnable spinnerUpdate = () -> {
            String spinner = SPINNER_FRAMES[spinnerIndex.getAndIncrement() % SPINNER_FRAMES.length];
            String text = formatChecklist(steps, completed, stepIndex, spinner);
            message.editMessage(text).queue();
        };

        ScheduledFuture<?> spinnerTask = spinnerExec.scheduleAtFixedRate(spinnerUpdate, 0, 200, TimeUnit.MILLISECONDS);

        return runActualStep(stepIndex, completed).handle((res, ex) -> {
            spinnerTask.cancel(false);
            spinnerExec.shutdown();

            if (ex != null) {
                message.editMessage("Step failed: " + steps.get(stepIndex)).queue();
                return null;
            }

            completed.set(stepIndex, true);
            message.editMessage(formatChecklist(steps, completed, stepIndex, "✔")).queue();

            return runStepsSequentially(steps, completed, message, stepIndex + 1);
        }).thenCompose(step -> step == null ? CompletableFuture.completedFuture(null) : step);
    }


    private static String formatChecklist(List<String> steps, List<Boolean> completed, int currentStep, String spinner) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (mismatch) {
                steps = new ArrayList<>(steps.subList(0, 2));
                steps.add("Versions match");

                completed = new ArrayList<>(completed.subList(0, 3));
            }
            if (completed.get(i)) {
                sb.append(steps.get(i)).append((Objects.equals(steps.get(i), "Versions match") ? "" : " ✔\n"));
            } else if (i == currentStep) {
                sb.append(steps.get(i)).append(" ").append(spinner).append("\n");
                break; // do NOT show steps after current
            }
        }
        return sb.toString();
    }


    private static CompletableFuture<Void> runActualStep(int stepIndex, List<Boolean> completed) {
        switch (stepIndex) {
            case 0:
                return checkGit();
            case 1:
                return checkItch();
            case 2:
                if (!Objects.equals(itchVersion, gitVersion)) {
                    return downloadFiles();
                } else {
                    completed.set(2, true);
                    completed.set(3, true);
                    completed.set(4, true);
                    mismatch = true;
                    return CompletableFuture.completedFuture(null);
                }
            case 3:
                return createRelease();
            case 4:
                return uploadFiles();
            default:
                return CompletableFuture.completedFuture(null);
        }
    }
}
