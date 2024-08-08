/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.routing;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;

import java.util.concurrent.Callable;

/**
 * Callable creating a {@link RoutingProfile} from a name String,
 * a {@link ProfileProperties}, an {@link EngineProperties} and a {@link RoutingProfileLoadContext}.
 */
public class RoutingProfileLoader implements Callable<RoutingProfile> {
    private final String name;
    private final EngineProperties engineConfig;
    private final ProfileProperties profile;
    private final RoutingProfileLoadContext loadContext;

    public RoutingProfileLoader(String name, ProfileProperties profile, EngineProperties engineConfig, RoutingProfileLoadContext loadContext) {
        this.name = name;
        this.profile = profile;
        this.engineConfig = engineConfig;
        this.loadContext = loadContext;
    }

    @Override
    public RoutingProfile call() throws Exception {
        Thread.currentThread().setName("ORS-pl-" + name);
        return new RoutingProfile(profile, engineConfig, loadContext);
    }
}