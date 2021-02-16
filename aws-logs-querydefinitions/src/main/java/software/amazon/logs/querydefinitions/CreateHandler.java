package software.amazon.logs.querydefinitions;

import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutQueryDefinitionResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        PutQueryDefinitionResponse putQueryDefinitionResponse;
        try {
            putQueryDefinitionResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToCreateRequest(model), ClientBuilder.getLogsClient()::putQueryDefinition);
        } catch (InvalidParameterException ex) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
        }

        model.setQueryDefinitionId(putQueryDefinitionResponse.queryDefinitionId());
        final String createMessage = String.format("%s [%s] successfully created.", ResourceModel.TYPE_NAME, model.getName());
        logger.log(createMessage);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
