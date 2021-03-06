package com.zlscorp.ultragrav.communication.transfer;

import com.zlscorp.ultragrav.communication.CommunicationErrorType;

public interface ResponseCallback<R extends AbstractResponse> {
	
	/**
	 * When a Comm has received a response. May be EmptyResponse.
	 * NOTE: Called from a background looper. Impl should dispatch off the background looper.
	 */
	public void onResponse(R response);
	
	/**
	 * When a Comm has failed.
	 * NOTE: Called from a background looper. Impl should dispatch off the background looper.
	 */
	public void onFailed(CommunicationErrorType type);
}
