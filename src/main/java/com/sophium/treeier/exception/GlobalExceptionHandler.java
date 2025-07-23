package com.sophium.treeier.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

import static com.sophium.treeier.util.Constants.CANNOT_MOVE_NODE_TO_A_DESCENDANT_OF_ITSELF;
import static com.sophium.treeier.util.Constants.FAILED_TO_FIND_REQUESTED_ELEMENT;
import static com.sophium.treeier.util.Constants.MAXIMUM_DEPTH_LIMIT_REACHED;
import static com.sophium.treeier.util.Constants.MAXIMUM_NODES_LIMIT_REACHED;
import static com.sophium.treeier.util.Constants.MOVE_NODE_TO_ITSELF;
import static com.sophium.treeier.util.Constants.NODE_DOES_NOT_EXIST_IN_THIS_TREE;
import static com.sophium.treeier.util.Constants.NODE_ID_ALREADY_EXISTS;
import static com.sophium.treeier.util.Constants.REQUIRED_FIELD;
import static com.sophium.treeier.util.Constants.UNKNOWN_ERROR_OCCURRED;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String TRACE = "trace";

    @Value("${reflectoring.trace:false}")
    private boolean printStackTrace;

    @Override
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Validation error. Check 'errors' field for details.");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    @ExceptionHandler(DepthLimitException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> depthLimitReachedException(DepthLimitException depthLimitException, WebRequest request) {
        log.error(MAXIMUM_DEPTH_LIMIT_REACHED, depthLimitException);
        return buildErrorResponse(depthLimitException, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NodeLimitException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> nodesLimitReachedException(NodeLimitException depthLimitException, WebRequest request) {
        log.error(MAXIMUM_NODES_LIMIT_REACHED, depthLimitException);
        return buildErrorResponse(depthLimitException, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NoSuchElementFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleNoSuchElementFoundException(NoSuchElementFoundException elementNotFoundException, WebRequest request) {
        log.error(FAILED_TO_FIND_REQUESTED_ELEMENT, elementNotFoundException);
        return buildErrorResponse(elementNotFoundException, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException accessDeniedException, WebRequest request) {
        return buildErrorResponse(accessDeniedException, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(CyclicalTreeStructureException.class)
    @ResponseStatus(HttpStatus.LOOP_DETECTED)
    public ResponseEntity<Object> handleCyclicalTreeStructure(CyclicalTreeStructureException ex, WebRequest request) {
        log.error(CANNOT_MOVE_NODE_TO_A_DESCENDANT_OF_ITSELF, ex);
        return buildErrorResponse(ex, HttpStatus.LOOP_DETECTED, request);
    }

    @ExceptionHandler(MoveAttemptToSelfException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Object> handleMoveToSelf(MoveAttemptToSelfException ex, WebRequest request) {
        log.error(MOVE_NODE_TO_ITSELF, ex);
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(NodeAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Object> handleNodeExists(NodeAlreadyExistsException ex, WebRequest request) {
        log.error(NODE_ID_ALREADY_EXISTS, ex);
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(InvalidNodeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleInvalidNode(InvalidNodeException ex, WebRequest request) {
        log.error(NODE_DOES_NOT_EXIST_IN_THIS_TREE, ex);
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(RequiredFieldException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleRequiredField(RequiredFieldException ex, WebRequest request) {
        log.error(REQUIRED_FIELD, ex);
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleAllUncaughtException(Exception exception, WebRequest request) {
        log.error(UNKNOWN_ERROR_OCCURRED, exception);
        return buildErrorResponse(exception, UNKNOWN_ERROR_OCCURRED, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<Object> buildErrorResponse(Exception exception,
                                                      HttpStatusCode httpStatus,
                                                      WebRequest request) {
        return buildErrorResponse(exception, exception.getMessage(), httpStatus, request);
    }

    private ResponseEntity<Object> buildErrorResponse(Exception exception,
                                                      String message,
                                                      HttpStatusCode httpStatus,
                                                      WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), message);
        if (printStackTrace && isTraceOn(request)) {
            errorResponse.setStackTrace(ExceptionUtils.getStackTrace(exception));
        }
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    private boolean isTraceOn(WebRequest request) {
        String[] value = request.getParameterValues(TRACE);
        return Objects.nonNull(value)
            && value.length > 0
            && value[0].contentEquals("true");
    }

    @Nullable
    @Override
    public ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        @Nullable Object body,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request) {

        return buildErrorResponse(ex, status, request);
    }
}
