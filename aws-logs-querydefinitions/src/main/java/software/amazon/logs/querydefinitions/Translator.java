package software.amazon.logs.querydefinitions;

import software.amazon.awssdk.services.cloudwatchlogs.model.PutQueryDefinitionRequest;

final class Translator {

    static PutQueryDefinitionRequest translateToCreateRequest(final ResourceModel model) {
        return PutQueryDefinitionRequest.builder()
                .name(model.getName())
                .queryString(model.getQueryString())
                .logGroupNames(model.getLogGroupNames())
                .build();
    }
}
