/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.content.impl;

import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.WCMMode;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AssetResolverImpl implements AssetResolver {
    private static final Logger log = LoggerFactory.getLogger(AssetResolverImpl.class);

    public Asset resolveAsset(final SlingHttpServletRequest request) {
        Asset asset = null;

        final Resource suffixResource = request.getRequestPathInfo().getSuffixResource();
        if (suffixResource != null) {
            asset = suffixResource.adaptTo(Asset.class);
        }

        if (asset == null) {
            final Resource requestResource = request.getResource();
            asset = requestResource.adaptTo(Asset.class);
        }

        if (asset == null) {
            if (!WCMMode.DISABLED.equals(WCMMode.fromRequest(request))) {
                asset = resolvePlaceholderAsset(request.adaptTo(Config.class));
            }

            if (asset == null) {
                throw new IllegalArgumentException(String.format("Unable to adapt request [ %s ] via a SlingHttpServletRequest into an Asset.", request.getRequestURI()));
            }
        }

        return asset;
    }

    public Asset resolveAsset(final Resource assetResource) {
        final Asset asset = assetResource.adaptTo(Asset.class);

        if (asset == null) {
            throw new IllegalArgumentException(String.format("Unable to adapt resource [ %s ] via a Resource into an Asset.", assetResource.getPath()));
        }

        return asset;
    }

    public Asset resolvePlaceholderAsset(final Config config) {
        log.debug("Attempting to construct a placeholder AssetModel for [ {} ]", config.getRootPath());

        final AssetModel placeholder = config.getPlaceholderAsset();

        if (placeholder != null && placeholder.getResource() != null) {
            return placeholder.getResource().adaptTo(Asset.class);
        }

        return null;
    }
}
