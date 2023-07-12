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
package org.xwiki.extension.index.internal.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contains the maps of all the CVEs with available reviews.
 *
 * @version $Id$
 * @since 15.6RC1
 */
public class ReviewsMap
{
    private final Map<String, List<Review>> reviewsMap = new HashMap<>();

    /**
     * @return the map of CVEs and their associated reviews.
     */
    public Map<String, List<Review>> getReviewsMap()
    {
        return this.reviewsMap;
    }

    /**
     * @param id a CVE id
     * @return {@code true} if at least a review is available for a given id
     */
    public boolean contains(String id)
    {
        return this.reviewsMap.containsKey(id);
    }

    /**
     * @param id a CVE id
     * @return the list of reviews if found, {@link Optional#empty()} otherwise
     */
    public Optional<List<Review>> getById(String id)
    {
        return Optional.ofNullable(this.reviewsMap.get(id));
    }
}
