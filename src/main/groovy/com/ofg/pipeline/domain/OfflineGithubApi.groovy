package com.ofg.pipeline.domain

class OfflineGithubApi {
    public static final String API = OfflineGithubApi.getResource('/offline.json').text
}
