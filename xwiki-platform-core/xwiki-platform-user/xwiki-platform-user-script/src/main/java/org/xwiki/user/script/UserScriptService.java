/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.user.script;

import java.io.IOException;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.namespace.QName;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;
import org.xwiki.script.service.ScriptServiceManager;
import org.xwiki.stability.Unstable;
import org.xwiki.user.ActorSearchResult;
import org.xwiki.user.ActorSearchResults;
import org.xwiki.user.ActorsSearchService;
import org.xwiki.user.CurrentUserReference;
import org.xwiki.user.GuestUserReference;
import org.xwiki.user.SuperAdminUserReference;
import org.xwiki.user.UserManager;
import org.xwiki.user.UserProperties;
import org.xwiki.user.UserPropertiesResolver;
import org.xwiki.user.UserReference;
import org.xwiki.user.UserReferenceResolver;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

/**
 * Users related script API.
 *
 * @version $Id$
 * @since 10.8RC1
 */
@Component
@Named(UserScriptService.ROLEHINT)
@Singleton
public class UserScriptService implements ScriptService
{
    /**
     * The role hint of this component.
     */
    public static final String ROLEHINT = "user";

    @Inject
    @Named("secure")
    private UserPropertiesResolver userPropertiesResolver;

    @Inject
    @Named("secure/all")
    private UserPropertiesResolver allUserPropertiesResolver;

    @Inject
    private ScriptServiceManager scriptServiceManager;

    @Inject
    private UserManager userManager;

    @Inject
    private UserReferenceResolver<String> userReferenceResolver;

    @Inject
    private ActorsSearchService actorsSearchService;

    /**
     * @param <S> the type of the {@link ScriptService}
     * @param serviceName the name of the sub {@link ScriptService}
     * @return the {@link ScriptService} or null of none could be found
     */
    @SuppressWarnings("unchecked")
    public <S extends ScriptService> S get(String serviceName)
    {
        return (S) this.scriptServiceManager.get(ROLEHINT + '.' + serviceName);
    }

    /**
     * @param userReference the reference to the user properties to resolve
     * @param parameters optional parameters that have a meaning only for the specific resolver implementation used
     * @return the User Properties object
     * @since 12.2
     */
    @Unstable
    public UserProperties getProperties(UserReference userReference, Object... parameters)
    {
        return this.userPropertiesResolver.resolve(userReference, parameters);
    }

    /**
     * Note that we have a {@code UserReferenceConverter} component to automatically convert from
     * String to {@link UserReference} but since in the signature we accept a vararg of Object, the
     * {@link #getProperties(Object...)} is called instead when a single string is passed. This is the reason for this
     * method, so that it's called when a String is passed.
     *
     * @param userReference the reference to the user properties to resolve.
     * @param parameters optional parameters that have a meaning only for the specific resolver implementation used
     * @return the User Properties object
     * @since 12.3RC1
     */
    @Unstable
    public UserProperties getProperties(String userReference, Object... parameters)
    {
        return this.userPropertiesResolver.resolve(this.userReferenceResolver.resolve(userReference), parameters);
    }

    /**
     * @param parameters optional parameters that have a meaning only for the specific resolver implementation used
     * @return the User Properties object for the current user
     * @since 12.2
     */
    @Unstable
    public UserProperties getProperties(Object... parameters)
    {
        return this.userPropertiesResolver.resolve(CurrentUserReference.INSTANCE, parameters);
    }

    /**
     * @return the User Properties object for the current user
     * @since 12.2
     */
    @Unstable
    public UserProperties getProperties()
    {
        return this.userPropertiesResolver.resolve(CurrentUserReference.INSTANCE);
    }

    /**
     * @param userReference the reference to the user properties to resolve
     * @param parameters optional parameters that have a meaning only for the specific resolver implementation used
     * @return the User Properties object
     * @since 12.2
     */
    @Unstable
    public UserProperties getAllProperties(UserReference userReference, Object... parameters)
    {
        return this.allUserPropertiesResolver.resolve(userReference, parameters);
    }

    /**
     * @param parameters optional parameters that have a meaning only for the specific resolver implementation used
     * @return the User Properties object for the current user
     * @since 12.2
     */
    @Unstable
    public UserProperties getAllProperties(Object... parameters)
    {
        return this.allUserPropertiesResolver.resolve(CurrentUserReference.INSTANCE, parameters);
    }

    /**
     * @return the User Properties object for the current user
     * @since 12.2
     */
    @Unstable
    public UserProperties getAllProperties()
    {
        return this.allUserPropertiesResolver.resolve(CurrentUserReference.INSTANCE);
    }

    /**
     * @return the Guest User reference
     * @since 12.2
     */
    @Unstable
    public UserReference getGuestUserReference()
    {
        return GuestUserReference.INSTANCE;
    }

    /**
     * @return the SuperAdmin User reference
     * @since 12.2
     */
    @Unstable
    public UserReference getSuperAdminUserReference()
    {
        return SuperAdminUserReference.INSTANCE;
    }

    /**
     * @return the current User reference
     * @since 12.2
     */
    @Unstable
    public UserReference getCurrentUserReference()
    {
        return CurrentUserReference.INSTANCE;
    }

    /**
     * @param userReference the reference to the user to test for existence (i.e. if the user pointed to by the
     *                      reference exists or not - for example the superadmin users or the guest users don't exist,
     *                      and a "document"-based User can be constructed and have no profile page and thus not exist)
     * @return true if the user exists in the store or false otherwise
     * @since 12.2
     */
    @Unstable
    public boolean exists(UserReference userReference)
    {
        return this.userManager.exists(userReference);
    }

    /**
     * @param uorgs TODO
     * @param wiki TODO
     * @param input TODO
     * @param limit TODO
     * @param format the expected format of the result, can be {@code json} or {@code xml}
     * @return
     * @since 12.10RC1
     */
    @Unstable
    public String search(String uorgs, WikiReference wiki, String input, long limit, String format)
    {
        ActorSearchResults search = this.actorsSearchService.search(uorgs, wiki, input, limit);
        if (Objects.equals(format, "json")) {
            try {
                // TODO: make object mapper a constant?
                // TODO: or extract a search result serializer component?
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(search.getResults());
            } catch (IOException e) {
                // TODO handle error

            }
        } else if (Objects.equals(format, "xml")) {
            JacksonXmlModule module = new JacksonXmlModule();
            module.setDefaultUseWrapper(false);
            module.addSerializer(ActorSearchResults.class, new JsonSerializer<ActorSearchResults>()
            {
                @Override public void serialize(ActorSearchResults actorSearchResult, JsonGenerator jsonGenerator,
                    SerializerProvider serializerProvider) throws IOException
                {
                    ToXmlGenerator toXmlGenerate = (ToXmlGenerator) jsonGenerator;
                    toXmlGenerate.setNextName(QName.valueOf("results"));
                    toXmlGenerate.writeStartObject();
                    for (ActorSearchResult result : actorSearchResult.getResults()) {
                        toXmlGenerate.writeFieldName("rs");
                        toXmlGenerate.writeStartObject();
                        toXmlGenerate.setNextIsAttribute(true);
                        toXmlGenerate.writeStringField("id", result.getUrl());
//                        toXmlGenerate.setNextIsAttribute(true);
                        toXmlGenerate.writeStringField("info", result.getLabel());
                        toXmlGenerate.writeStringField("type", result.getType());
                        toXmlGenerate.setNextIsAttribute(false);
                        toXmlGenerate.setNextIsUnwrapped(true);
                        toXmlGenerate.writeStringField("", result.getValue());
                    }
                    jsonGenerator.writeEndObject();
                }
            });
            XmlMapper xmlMapper = new XmlMapper(module);
            try {
                return xmlMapper.writeValueAsString(search);
            } catch (JsonProcessingException e) {
                // TODO handle error
            }
        }
        return null;
    }
}
