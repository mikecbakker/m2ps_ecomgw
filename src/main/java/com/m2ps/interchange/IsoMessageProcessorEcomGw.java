
package com.m2ps.interchange;

import java.io.IOException;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;

import com.m2ps.message.M2PSIsoMsg;
import com.m2ps.message.M2PSIsoMsg.MsgType;

/**
 * 
 * Ecommerce gateway interchange
 * 
 */
public class IsoMessageProcessorEcomGw extends IsoMessageProcessor
{
	/* Used to determine whether this entity is signed on */
	private boolean signedOn = true;
	
	/*-----------------------------------------------------------------------------------------------------------------*/
	/**
	 * Constructs a new <code>IsoSrcRequestListener</code> instance.
	 */
	public IsoMessageProcessorEcomGw()
	{
	}
	
	/**
	 * Retrieves and loads the configuration as defined in the QBean.
	 * 
	 * @param cfg
	 * @throws ConfigurationException
	 * @see org.jpos.core.Configurable#setConfiguration(org.jpos.core.Configuration)
	 */
	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException
	{
		super.setConfiguration(cfg);
	}
	
	/**
	 * This function performs translation of a request message from one zone to another.
	 * 
	 * @param request
	 *           This is the input or source message. i.e. The translation function will use this as
	 *           the reference when translating.
	 * @param translatedRequest
	 *           This is the output of the translation operation. The translatedRequest will be the
	 *           final output of the request translation.
	 * @throws ISOException
	 * @see com.m2ps.interchange.IsoMessageProcessor#performRequestTranslation(org.jpos.iso.ISOMsg,
	 *      org.jpos.iso.ISOMsg)
	 */
	@Override
	public void performRequestTranslation(ISOMsg request, ISOMsg translatedRequest)
		throws ISOException,
			IOException
	{
		info("[IsoMessageProcessor] Performing request translation");
		
		/* Not signed on. So want to send message to source declining with response code 91 */
		if (!signedOn)
		{
			ISOSource isoSource = request.getSource();
			ISOMsg response = request;
			response.setMTI(M2PSIsoMsg.MsgType.getResponseMsgType(request.getMTI()));
			response.set(
				M2PSIsoMsg.Bit._039_RESPONSE_CODE,
				M2PSIsoMsg.RspCode._91_ISSUER_SWITCH_INOPERATIVE);
			isoSource.send(response);
			return;
		}
		
		/* Signed on so process request message. */
		String msgType = request.getMTI();
		
		if (MsgType._0200_TRAN_REQ.equals(msgType))
		{
			processTranRequest(request, translatedRequest);
		}
	}
	
	/**
	 * 
	 * This function performs translation of a response message from one zone to another.
	 * 
	 * @param response
	 *           This is the input or source message. i.e. The translation function will use this as
	 *           the reference when translating.
	 * @param translatedResponse
	 *           This is the output of the translation operation. The translatedResponse will be the
	 *           final output of the response translation.
	 * @throws ISOException
	 * @see com.m2ps.interchange.IsoMessageProcessor#performResponseTranslation(org.jpos.iso.ISOMsg,
	 *      org.jpos.iso.ISOMsg)
	 */
	@Override
	public void performResponseTranslation(ISOMsg response, ISOMsg translatedResponse)
		throws ISOException
	{
		info("[IsoMessageProcessor] Performing response translation");
		String msgType = response.getMTI();
		
		if (MsgType._0210_TRAN_REQ_RSP.equals(msgType))
		{
			processTranRequestResponse(response, translatedResponse);
		}
	}
	
	/**
	 * 
	 * Mappings for the 0200 Transaction Request
	 * 
	 * @param request
	 * @param translatedRequest
	 */
	public void processTranRequest(ISOMsg request, ISOMsg translatedRequest) throws ISOException
	{
		translatedRequest = new ISOMsg();
		translatedRequest.setMTI(MsgType._0200_TRAN_REQ);
		
		/* Straight copy fields */
		M2PSIsoMsg.copyField(translatedRequest, request, M2PSIsoMsg.Bit._002_PAN);
		M2PSIsoMsg.copyField(translatedRequest, request, M2PSIsoMsg.Bit._003_PROCESING_CODE);
		M2PSIsoMsg.copyField(translatedRequest, request, M2PSIsoMsg.Bit._004_AMOUNT_TRAN);
		M2PSIsoMsg.copyField(translatedRequest, request, M2PSIsoMsg.Bit._011_SYSTEM_TRACE_AUDIT_NR);
		M2PSIsoMsg.copyField(
			translatedRequest,
			request,
			M2PSIsoMsg.Bit._041_CARD_ACCEPTOR_TERMINAL_ID);
		
		/* Constructed fields */
	}
	
	/**
	 * 
	 * Mappings for the 0210 Transaction Request Response
	 * 
	 * @param response
	 * @param translatedResponse
	 */
	public void processTranRequestResponse(ISOMsg response, ISOMsg translatedResponse)
		throws ISOException
	{
		translatedResponse = new ISOMsg();
		translatedResponse.setMTI(MsgType._0210_TRAN_REQ_RSP);
		
		/* Straight copy fields */
		M2PSIsoMsg.copyField(translatedResponse, response, M2PSIsoMsg.Bit._002_PAN);
		M2PSIsoMsg.copyField(translatedResponse, response, M2PSIsoMsg.Bit._003_PROCESING_CODE);
		M2PSIsoMsg.copyField(translatedResponse, response, M2PSIsoMsg.Bit._004_AMOUNT_TRAN);
		M2PSIsoMsg.copyField(translatedResponse, response, M2PSIsoMsg.Bit._011_SYSTEM_TRACE_AUDIT_NR);
		M2PSIsoMsg.copyField(translatedResponse, response, M2PSIsoMsg.Bit._039_RESPONSE_CODE);
		M2PSIsoMsg.copyField(
			translatedResponse,
			response,
			M2PSIsoMsg.Bit._041_CARD_ACCEPTOR_TERMINAL_ID);
		
		/* Constructed fields */
	}
}
