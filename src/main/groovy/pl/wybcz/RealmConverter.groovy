package pl.wybcz


interface RealmConverter {
    Map<String, List<String>> convertToRealmMultimap(List<GitProject> projectToCode)
}