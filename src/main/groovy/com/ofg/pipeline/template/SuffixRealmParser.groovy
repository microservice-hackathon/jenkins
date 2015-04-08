package com.ofg.pipeline.template

import com.ofg.pipeline.domain.GitProject

class SuffixRealmParser implements RealmConverter {

    @Override
    Map<String, List<String>> convertToRealmMultimap(List<GitProject> projectToCode) {
        Map emptyMultimap = [:].withDefault { [] }
        return projectToCode.inject(emptyMultimap) { Map acc, GitProject project ->
            String realm = project.name.split('-').last()
            acc[realm] << project.name
            return acc
        }
    }
}
