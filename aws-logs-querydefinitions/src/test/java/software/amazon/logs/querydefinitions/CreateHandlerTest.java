package software.amazon.logs.querydefinitions;

import com.google.common.collect.ImmutableList;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutQueryDefinitionResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {
    private static final String MOCK_QUERYDEF_ID = "someId";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    PutQueryDefinitionResponse putQueryDefinitionResponse;

    ImmutableList<ResourceModel> modelsUnderTestValid = ImmutableList.of(
            ResourceModel.builder().name("myQuery").queryString("filter @message like /ERROR/").build(),
            ResourceModel.builder().name("myQuery").queryString("filter @message like /ERROR/").logGroupNames(ImmutableList.of()).build(),
            ResourceModel.builder().name("myQuery").queryString("filter @message like /ERROR/").logGroupNames(ImmutableList.of("LG1")).build(),
            ResourceModel.builder().name("myQuery").queryString("filter @message like /ERROR/").logGroupNames(ImmutableList.of("LG1", "LG2")).build()
    );

    ImmutableList<ResourceModel> modelsUnderTestInvalid = ImmutableList.of(
            ResourceModel.builder().queryString("filter @message like /ERROR/").build()
    );

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        putQueryDefinitionResponse = PutQueryDefinitionResponse.builder()
                .queryDefinitionId(MOCK_QUERYDEF_ID)
                .build();
    }


    @Test
    public void handleRequest_Success_ValidCases() {
        final CreateHandler handler = new CreateHandler();

        doReturn(putQueryDefinitionResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        for (ResourceModel model : modelsUnderTestValid) {
            ResourceModel expectedModel = ResourceModel.builder()
                    .name(model.getName())
                    .queryString(model.getQueryString())
                    .logGroupNames(model.getLogGroupNames())
                    .queryDefinitionId(MOCK_QUERYDEF_ID)
                    .build();
            final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                    .desiredResourceState(expectedModel)
                    .build();

            final ProgressEvent<ResourceModel, CallbackContext> response
                    = handler.handleRequest(proxy, request, null, logger);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
            assertThat(response.getCallbackContext()).isNull();
            assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
            assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
            assertThat(response.getResourceModels()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getErrorCode()).isNull();
        }
    }

    @Test
    public void handleRequest_Failure_InvalidRequest() {
        final CreateHandler handler = new CreateHandler();

        ResourceModel model = ResourceModel.builder().queryString("filter @message like /ERROR/").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(InvalidParameterException.builder().message("test Error").build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }
}
