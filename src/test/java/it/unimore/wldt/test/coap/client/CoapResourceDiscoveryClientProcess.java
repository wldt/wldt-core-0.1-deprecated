package it.unimore.wldt.test.coap.client;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;


public class CoapResourceDiscoveryClientProcess {

	private final static Logger logger = LoggerFactory.getLogger(CoapResourceDiscoveryClientProcess.class);

	private static final String COAP_ENDPOINT = "coap://127.0.0.1:5783/.well-known/core";

	public static void main(String[] args) {
		
		//Initialize coapClient
		CoapClient coapClient = new CoapClient(COAP_ENDPOINT);

		//Request Class is a generic CoAP message: in this case we want a GET.
		//"Message ID", "Token" and other header's fields can be set 
		Request request = new Request(Code.GET);

		//Set Request as Confirmable
		request.setConfirmable(true);

		logger.info("Request Pretty Print: \n{}", Utils.prettyPrint(request));

		//Synchronously send the GET message (blocking call)
		CoapResponse coapResp = null;

		try {

			coapResp = coapClient.advanced(request);

			if(coapResp != null) {

				//Pretty print for the received response
				logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));

				if (coapResp.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {

					Set<WebLink> links = LinkFormat.parse(coapResp.getResponseText());

					links.forEach(link -> {

						logger.info(String.format("Link: %s", link.getURI()));

						link.getAttributes().getAttributeKeySet().stream().forEach(attributeKey -> {
							logger.info(String.format("Attribute (%s): %s", attributeKey, link.getAttributes().getAttributeValues(attributeKey)));
						});

					});

				} else {
					logger.error("CoRE Link Format Response not found !");
				}
			}
		} catch (ConnectorException | IOException e) {
			e.printStackTrace();
		}
	}
}