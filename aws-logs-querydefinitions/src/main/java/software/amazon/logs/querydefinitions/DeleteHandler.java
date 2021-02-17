package software.amazon.logs.querydefinitions;

import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteQueryDefinitionResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();


        DeleteQueryDefinitionResponse deleteQueryDefinitionResponse;
        try {
            deleteQueryDefinitionResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToDeleteRequest(model), ClientBuilder.getLogsClient()::deleteQueryDefinition);
        } catch (InvalidParameterException ex) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        } catch (ServiceUnavailableException ex) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
        }
        OperationStatus status = deleteQueryDefinitionResponse.success() ? OperationStatus.SUCCESS : OperationStatus.FAILED;
        final String message = String.format("%s [%s] successfully deleted.", ResourceModel.TYPE_NAME, model.getName());
        logger.log(message);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(status)
            .build();
    }
}
