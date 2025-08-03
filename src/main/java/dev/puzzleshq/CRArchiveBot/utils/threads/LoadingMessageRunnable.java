package dev.puzzleshq.CRArchiveBot.utils.threads;

import net.dv8tion.jda.api.interactions.InteractionHook;

public class LoadingMessageRunnable implements Runnable {
    InteractionHook hook;
    boolean checkingGit = false;
    boolean checkingItch = false;
    boolean downloadingFiles = false;
    boolean creatingRelease = false;
    boolean uploadingFiles = false;

    public LoadingMessageRunnable(InteractionHook hook) {
        this.hook = hook;
    }

    //TODO get this working because i cant be bloody asked right now
    @Override
    public void run() {
        char[] chars = {'|', '/', '⟋', '—', '⟍','\\'};
        int cInc = 1;
        while (checkingGit && checkingItch && downloadingFiles && creatingRelease && uploadingFiles) {
            if (cInc % 2 == 0) continue;
            char chr = chars[cInc -1];
            hook.editOriginal("Checking versions " + chr).queue();
            cInc++;
            if (cInc == chars.length) cInc = 1;
        }
    }



    public void setHook(InteractionHook hook) {
        this.hook = hook;
    }

    public void setCheckingGit(boolean checkingGit) {
        this.checkingGit = checkingGit;
    }

    public void setCheckingItch(boolean checkingItch) {
        this.checkingItch = checkingItch;
    }

    public void setDownloadingFiles(boolean downloadingFiles) {
        this.downloadingFiles = downloadingFiles;
    }

    public void setCreatingRelease(boolean creatingRelease) {
        this.creatingRelease = creatingRelease;
    }

    public void setUploadingFiles(boolean uploadingFiles) {
        this.uploadingFiles = uploadingFiles;
    }

    public InteractionHook getHook() {
        return hook;
    }

    public boolean isCheckingGit() {
        return checkingGit;
    }

    public boolean isCheckingItch() {
        return checkingItch;
    }

    public boolean isDownloadingFiles() {
        return downloadingFiles;
    }

    public boolean isCreatingRelease() {
        return creatingRelease;
    }

    public boolean isUploadingFiles() {
        return uploadingFiles;
    }
}
