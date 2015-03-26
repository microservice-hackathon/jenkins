package com.dslexample

import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.MemoryJobManagement

trait JobSpecTrait {

    JobParent createJobParent() {
        JobParent jp = new StubbedJobParent()
        JobManagement jm = new MemoryJobManagement()
        jp.setJm(jm)
        return jp
    }

    static class StubbedJobParent extends JobParent {
        @Override
        Object run() {
            return null
        }
    }
}
