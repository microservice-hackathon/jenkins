package com.ofg.pipeline.template

import com.ofg.pipeline.domain.GitProject

interface RealmConverter {
    Map<String, List<String>> convertToRealmMultimap(List<GitProject> projectToCode)
}