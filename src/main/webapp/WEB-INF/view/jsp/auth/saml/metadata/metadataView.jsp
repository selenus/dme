<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<c:set value="/auth/saml/admin" var="pathPrefix" />
<c:set value="/saml/metadata" var="ssoMetadataPrefix" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span12 content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
				<li><a href="<c:url value="/"/>">Start</a> <span class="divider">/</span></li>
			   	<li><a href="<c:url value="${pathPrefix}"/>">Metadata</a> <span class="divider">/</span></li>
			   	<li class="active">Metadata Detail</li>
			</ul>
			<h1>Metadata detail</h1>
			<sf:form class="form-horizontal form-horizontal-long-labels" commandName="metadata">
				<fieldset>
					<div class="control-group">
						<div class="controls">
							<c:choose>
					            <c:when test="${metadata.alias != null}">
					                <a class="btn btn-primary" href="<c:url value="${ssoMetadataPrefix}/alias/${metadata.alias}"/>">Download metadata</a>
					            </c:when>
					            <c:otherwise>
					                <a class="btn btn-primary" href="<c:url value="${ssoMetadataPrefix}"/>">Download metadata</a>
					            </c:otherwise>
				        	</c:choose>
				        	<a class="btn" href="<c:url value="${pathPrefix}"/>">Cancel</a>
				        </div>
					</div>
				    
				    <div class="control-group">
						<label class="control-label" for="saml-metadata-entityId">Entity ID:</label>
						<div class="controls">
							<sf:input class="input-xlarge" readonly="true" id="saml-metadata-entityId" path="entityId"/>
						</div>
					</div>
					
					<div class="control-group">
						<label class="control-label" for="saml-metadata-alias">Entity alias:</label>
						<div class="controls">
							<sf:input class="input-medium" readonly="true" id="saml-metadata-alias" path="alias"/>
						</div>
					</div>
		
					<div class="control-group">
						<label class="control-label" for="saml-metadata-securityProfile">Security profile:</label>
						<div class="controls">
							<sf:select class="input-medium" id="saml-metadata-securityProfile" path="securityProfile" disabled="true" multiple="false">
				            	<sf:option value="metaiop">MetaIOP</sf:option>
				                <sf:option value="pkix">PKIX</sf:option>
				            </sf:select>
						</div>
					</div>
					
					<div class="control-group">
						<label class="control-label" for="saml-metadata-sslSecurityProfile">SSL/TLS Security profile:</label>
						<div class="controls">
				            <sf:select class="input-medium" id="saml-metadata-sslSecurityProfile" path="sslSecurityProfile" disabled="true" multiple="false">
				            	<sf:option value="pkix">PKIX</sf:option>
				                <sf:option value="metaiop">MetaIOP</sf:option>
				            </sf:select>
						</div>
					</div>
					
					<div class="control-group">
						<label class="control-label" for="saml-metadata-signingKey">Signing key:</label>
						<div class="controls">
							<sf:input class="input-large" readonly="true" id="saml-metadata-signingKey" path="signingKey"/>
						</div>
					</div>
		
					<div class="control-group">
						<label class="control-label" for="saml-metadata-encryptionKey">Encryption key:</label>
						<div class="controls">
							<sf:input class="input-large" readonly="true" id="saml-metadata-encryptionKey" path="encryptionKey"/>
						</div>
					</div>		
		
					<div class="control-group">
						<label class="control-label" for="saml-metadata-tlsKey">SSL/TLS key:</label>
						<div class="controls">
							<sf:input class="input-large" readonly="true" id="saml-metadata-tlsKey" path="tlsKey"/>
						</div>
					</div>		
		
					<div class="control-group">
						<label class="control-label" for="saml-metadata-wantAssertionSigned">Require signed authentication Assertion:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-wantAssertionSigned" path="wantAssertionSigned" disabled="true" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>       
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="saml-metadata-requireLogoutRequestSigned">Require signed LogoutRequest:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-requireLogoutRequestSigned" path="requireLogoutRequestSigned" disabled="true" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>        
						</div>
					</div>	
					<div class="control-group">
						<label class="control-label" for="saml-metadata-requireLogoutResponseSigned">Require signed LogoutResponse:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-requireLogoutResponseSigned" path="requireLogoutResponseSigned" disabled="true" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>        
						</div>
					</div>		
					<div class="control-group">
						<label class="control-label" for="saml-metadata-requireArtifactResolveSigned">Require signed ArtifactResolve:</label>
						<div class="controls">
				            <sf:select class="input-small" id="saml-metadata-requireArtifactResolveSigned" path="requireArtifactResolveSigned" disabled="true" multiple="false">
				            	<sf:option value="true">Yes</sf:option>
								<sf:option value="false">No</sf:option>
				            </sf:select>      
						</div>
					</div>	
				
					<c:if test="${metadata.local eq true}">		
						<div class="control-group">
							<div class="controls">
					            Instructions:
					            <ul>
			                        <li>Store metadata content in file ${storagePath}</li>
			                        <li>Make sure to update your identity provider(s) with the generated metadata.</li>
			                        <li>Modify bean "metadata" in your securityContext.xml and include content from the
			                            configuration bellow
			                        </li>
					            </ul>
							</div>
						</div>	
					</c:if>
				        
					<div class="control-group">
						<label class="control-label" for="saml-metadata-metadata">Metadata:</label>
						<div class="controls">
				            <textarea class="input-xxlarge" id="saml-metadata-metadata" rows="15" cols="100" readonly="true"><c:out value="${metadata.serializedMetadata}"/></textarea>    
						</div>
					</div>	
					
				    <c:if test="${metadata.local eq true}">
						<div class="control-group">
							<label class="control-label" for="saml-metadata-configuration">Configuration:</label>
							<div class="controls">
					            <textarea class="input-xxlarge" id="saml-metadata-configuration" rows="15" cols="100" readonly="true"><c:out value="${metadata.configuration}"/></textarea>    
							</div>
						</div>			
					</c:if>
				
					<div class="control-group">
						<div class="controls">
							<c:choose>
					            <c:when test="${metadata.alias != null}">
					                <a class="btn btn-primary" href="<c:url value="${ssoMetadataPrefix}/alias/${metadata.alias}"/>">Download metadata</a>
					            </c:when>
					            <c:otherwise>
					                <a class="btn btn-primary" href="<c:url value="${ssoMetadataPrefix}"/>">Download metadata</a>
					            </c:otherwise>
				        	</c:choose>
				        	<a class="btn" href="<c:url value="${pathPrefix}"/>">Cancel</a>
				        </div>
					</div>
				
				</fieldset>
			</sf:form>
		</div>
	</div>
</div>