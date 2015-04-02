package pl.wybcz.pipeline.template

import pl.wybcz.pipeline.domain.GitProject

interface RealmConverter {
    Map<String, List<String>> convertToRealmMultimap(List<GitProject> projectToCode)
}