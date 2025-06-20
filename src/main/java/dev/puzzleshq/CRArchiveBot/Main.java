package dev.puzzleshq.CRArchiveBot;

import dev.puzzleshq.CRArchiveBot.utils.GithubUtils;

public class Main {

    public static void main(String[] args) {

        GithubUtils.getCRArchive().listReleases().forEach(System.out::println);
    }
}
