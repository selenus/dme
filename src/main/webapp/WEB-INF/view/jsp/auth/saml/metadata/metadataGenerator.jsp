<%@ page import="de.dariah.aai.javasp.web.controller.SamlMetadataController" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<c:set value="/auth/saml/admin" var="pathPrefix" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span12 content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
				<li><a href="<c:url value="/"/>">Start</a> <span class="divider">/</span></li>
			   	<li><a href="<c:url value="${pathPrefix}"/>">Metadata</a> <span class="divider">/</span></li>
			   	<li class="active">Metadata generation</li>
			</ul>
			<h1>Metadata generation</h1>
			<h4>Generates new metadata for service provider. Output can be used to configure your securityContext.xml descriptor.</h4>
			
			<sf:form class="form-horizontal form-horizontal-long-labels" commandName="metadata" action="create">
				<fieldset>
					<legend>General SP Information</legend>
					<div class="control-group">
						<div class="controls">
							<button type="submit" class="btn btn-primary">Generate metadata</button>
							<a class="btn" href="<c:url value="${pathPrefix}"/>">Cancel</a>
						</div>
					</div>	
					<div class="control-group">
						<label class="control-label" for="saml-metadata-store">Store for the current session:</label>
						<div class="controls">
							<sf:select class="input-small" id="saml-metadata-store" path="store" multiple="false">
				                <sf:option value="true">Yes</sf:option>
				                <sf:option value="false">No</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="store"/></span>
				            <span class="help-block">When set to true the generated metadata will be stored in the local metadata manager. The value will be available only until restart of the application server.</span>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-entityId">Entity ID:</label>
						<div class="controls">
							<sf:input class="input-xlarge" id="saml-metadata-entityId" path="entityId"/>
				            <span class="help-inline"><sf:errors class="error" path="entityId"/></span>
				            <span class="help-block">Entity ID is a unique identifier for an identity or service provider. Value is included in the generated metadata.</span>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-alias">Entity alias:</label>
						<div class="controls">
							<sf:input class="input-medium" id="saml-metadata-alias" path="alias"/>
				            <span class="help-inline"><sf:errors class="error" path="alias"/></span>
				            <span class="help-block">Alias is an internal mechanism allowing collocating multiple service providers on one server. Alias must be unique.</span>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-baseURL">Entity base URL:</label>
						<div class="controls">
							<sf:input class="input-xxlarge" id="saml-metadata-baseURL" path="baseURL"/>
				            <span class="help-inline"><sf:errors class="error" path="baseURL"/></span>
				            <span class="help-block">Base to generate URLs for this server. For example: https://myServer:443/saml-app. The public address your server will be accessed from should be used here.</span>
						</div>
					</div>
						
					<legend>Signature and Encryption</legend>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-securityProfile">Security profile:</label>
						<div class="controls">
							<sf:select class="input-medium" id="saml-metadata-securityProfile" path="securityProfile" multiple="false">
				            	<sf:option value="metaiop">MetaIOP</sf:option>
				                <sf:option value="pkix">PKIX</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="securityProfile"/></span>
				            <div class="help-block">
				            	Security profile determines how is trust of digital signatures handled:
				                <ul>
				                    <li>
				                        In <a href="http://wiki.oasis-open.org/security/SAML2MetadataIOP">MetaIOP</a> mode certificate is deemed
				                        valid when it's declared in the metadata or extended metadata of the peer entity. No validation of the certificate is
				                        performed (e.g. revocation) and no certificate chains are evaluated. The value is recommended as a default.
				                    </li>
				                    <li>
				                        PKIX profile verifies credentials against a set of trust anchors. Certificates present in the
				                        metadata or extended metadata of the peer entity are treated as trust anchors, together with all keys in
				                        the keystore. Certificate chains are verified in this mode.
				                    </li>
				            	</ul>
				            </div>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-sslSecurityProfile">SSL/TLS Security profile:</label>
						<div class="controls">
				            <sf:select class="input-medium" id="saml-metadata-sslSecurityProfile" path="sslSecurityProfile" multiple="false">
				            	<sf:option value="pkix">PKIX</sf:option>
				                <sf:option value="metaiop">MetaIOP</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="sslSecurityProfile"/></span>
				            <div class="help-block">
								SSL/TLS Security profile determines how is trust of peer's SSL/TLS certificate (e.g. during Artifact resolution) handled:
				                <ul>
				                    <li>
				                    PKIX profile verifies peer's certificate against a set of trust anchors. All certificates defined in metadata,
				                    extended metadata or present in the keystore are considered as trusted anchors (certification authorities)
				                    for PKIX validation.
				                    </li>
				                    <li>
				                    In MetaIOP mode server's SSL/TLS certificate is trusted when it's explicitly declared in metadata or extended metadata of
				                    the peer.
				                    </li>
				            	</ul>
							</div>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-signingKey">Signing key:</label>
						<div class="controls">
							<sf:select class="input-large" id="saml-metadata-signingKey" path="signingKey" items="${availableKeys}"/>
				            <span class="help-inline"><sf:errors class="error" path="signingKey"/></span>
				            <span class="help-block">Key used for digital signatures of SAML messages. Public key will be included in the metadata.</span>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-encryptionKey">Encryption key:</label>
						<div class="controls">
							<sf:select class="input-large" id="saml-metadata-encryptionKey" path="encryptionKey" items="${availableKeys}"/>
				            <span class="help-inline"><sf:errors class="error" path="encryptionKey"/></span>
				            <span class="help-block">Key used for digital encryption of SAML messages. Public key will be included in the metadata.</span>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-tlsKey">SSL/TLS Client authentication key:</label>
						<div class="controls">
							<sf:select class="input-large" id="saml-metadata-tlsKey" path="tlsKey">
				                <sf:options items="${availableKeys}"/>
				                <sf:option value="">None</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="tlsKey"/></span>
				            <span class="help-block">Key used to authenticate this instance for SSL/TLS connections.</span>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-signMetadata">Sign metadata:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-signMetadata" path="signMetadata" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="signMetadata"/></span>
				            <span class="help-block">If true the generated metadata will be digitally signed using the specified signature key.</span>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-requestSigned">Sign sent AuthNRequests:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-requestSigned" path="requestSigned" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="requestSigned"/></span>	        
						</div>
					</div>
					
					<legend>Required Signatures</legend>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-wantAssertionSigned">Require signed authentication Assertion:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-wantAssertionSigned" path="wantAssertionSigned" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="wantAssertionSigned"/></span>	        
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-requireLogoutRequestSigned">Require signed LogoutRequest:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-requireLogoutRequestSigned" path="requireLogoutRequestSigned" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="requireLogoutRequestSigned"/></span>	        
						</div>
					</div>	
					<div class="control-group">
						<label class="control-label" for="saml-metadata-requireLogoutResponseSigned">Require signed LogoutResponse:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-requireLogoutResponseSigned" path="requireLogoutResponseSigned" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="requireLogoutResponseSigned"/></span>	        
						</div>
					</div>		
					<div class="control-group">
						<label class="control-label" for="saml-metadata-requireArtifactResolveSigned">Require signed ArtifactResolve:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-requireArtifactResolveSigned" path="requireArtifactResolveSigned" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="requireArtifactResolveSigned"/></span>	        
						</div>
					</div>	
					
					<legend>SSO Bindings</legend>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-requireArtifactResolveSigned">Single sign-on bindings:</label>
						<div class="controls">
				            <table>
				                <tr><th>Default&nbsp;&nbsp;</th><th>Included&nbsp;&nbsp;</th><th>Name</th></tr>
				                <tr>
				                    <td><sf:radiobutton path="ssoDefaultBinding" value="<%= SamlMetadataController.AllowedSSOBindings.SSO_ARTIFACT %>" /></td>
				                    <td><sf:checkbox path="ssoBindings" value="<%= SamlMetadataController.AllowedSSOBindings.SSO_ARTIFACT %>" id="sso_0"/></td>
				                    <td><label for="sso_0">SSO Artifact</label></td>
				                </tr>
				                <tr>
				                    <td><sf:radiobutton path="ssoDefaultBinding" value="<%= SamlMetadataController.AllowedSSOBindings.SSO_POST %>" /></td>
				                    <td><sf:checkbox path="ssoBindings" value="<%= SamlMetadataController.AllowedSSOBindings.SSO_POST %>" id="sso_1"/></td>
				                    <td><label for="sso_1">SSO HTTP-POST</label></td>
				                </tr>
				                <tr>
				                    <td><sf:radiobutton path="ssoDefaultBinding" value="<%= SamlMetadataController.AllowedSSOBindings.SSO_PAOS %>" /></td>
				                    <td><sf:checkbox path="ssoBindings" value="<%= SamlMetadataController.AllowedSSOBindings.SSO_PAOS %>" id="sso_2"/></td>
				                    <td><label for="sso_2">SSO PAOS</label></td>
				                </tr>
				                <tr>
				                    <td><sf:radiobutton path="ssoDefaultBinding" value="<%= SamlMetadataController.AllowedSSOBindings.HOKSSO_ARTIFACT %>" /></td>
				                    <td><sf:checkbox path="ssoBindings" value="<%= SamlMetadataController.AllowedSSOBindings.HOKSSO_ARTIFACT %>" id="sso_3"/></td>
				                    <td><label for="sso_3">HoK SSO Artifact</label></td>
				                </tr>
				                <tr>
				                    <td><sf:radiobutton path="ssoDefaultBinding" value="<%= SamlMetadataController.AllowedSSOBindings.HOKSSO_POST %>" /></td>
				                    <td><sf:checkbox path="ssoBindings" value="<%= SamlMetadataController.AllowedSSOBindings.HOKSSO_POST %>" id="sso_4"/></td>
				                    <td><label for="sso_4">HoK SSO HTTP-POST</label></td>
				                </tr>
				            </table>	        
						</div>
					</div>	
					
					<legend>Name IDs</legend>
					<div class="control-group">
						<label class="control-label">Supported NameIDs:</label>
						<div class="controls">
				            <label class="checkbox"><sf:checkbox path="nameID" value="urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress" id="nameid_0" checked="checked" /> E-Mail</label>
				            <label class="checkbox"><sf:checkbox path="nameID" value="urn:oasis:names:tc:SAML:2.0:nameid-format:transient" id="nameid_1" checked="checked" /> Transient</label>
				            <label class="checkbox"><sf:checkbox path="nameID" value="urn:oasis:names:tc:SAML:2.0:nameid-format:persistent" id="nameid_2" checked="checked" /> Persistent</label>
				            <label class="checkbox"><sf:checkbox path="nameID" value="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified" id="nameid_3" checked="checked" /> Unspecified</label>
				            <label class="checkbox"><sf:checkbox path="nameID" value="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName" id="nameid_4" checked="checked" /> X509 Subject</label>
				            <span class="help-inline"><sf:errors class="error" path="nameID"/></span>	        
						</div>
					</div>		
					
					<legend>IDP Discovery</legend>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-includeDiscovery">Include IDP Discovery profile:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-includeDiscovery" path="includeDiscovery" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>
				            <span class="help-inline"><sf:errors class="error" path="includeDiscovery"/></span>	 
				            <span class="help-block"><a href="http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-idp-discovery.pdf">Discovery profile</a> enables service provider to determine which identity provider should be used for a particular user. Spring Security SAML contains it's own discovery service which presents user with an IDP list to select from.</span>
						</div>
					</div>	
					<div class="control-group">
						<label class="control-label" for="saml-metadata-customDiscoveryURL">Custom URL for IDP Discovery:</label>
						<div class="controls">
				            <sf:input class="input-xxlarge" id="saml-metadata-customDiscoveryURL" path="customDiscoveryURL"/>
				            <span class="help-inline"><sf:errors class="error" path="customDiscoveryURL"/></span>	 
				            <span class="help-block">When not set local IDP discovery URL is automatically generated when IDP discovery is enabled.</span>
						</div>
					</div>
					<div class="control-group">
						<div class="controls">
							<button type="submit" class="btn btn-primary">Generate metadata</button>
							<a class="btn" href="<c:url value="${pathPrefix}"/>">Cancel</a>
						</div>
					</div>	
				</fieldset>
			</sf:form>
		</div>
	</div>
</div>