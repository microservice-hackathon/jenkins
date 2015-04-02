package pl.wybcz.pipeline.template

import pl.wybcz.pipeline.domain.GitProject

class SuffixRealmParser implements RealmConverter {

    @Override
    Map<String, List<String>> convertToRealmMultimap(List<GitProject> projectToCode) {
        Map emptyMultimap = [:].withDefault { [] }
        return projectToCode.inject(emptyMultimap) { Map acc, entry ->
            String realm = entry.name.split('-').last()
            acc[realm] << entry.name
            return acc
        }
    }
}
