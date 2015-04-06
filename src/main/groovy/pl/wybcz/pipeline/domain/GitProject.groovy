package pl.wybcz.pipeline.domain

import groovy.transform.Immutable

@Immutable
class GitProject {
    String name
    String cloneUrl
}
