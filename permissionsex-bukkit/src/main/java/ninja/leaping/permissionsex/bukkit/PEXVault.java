/**
 * PermissionsEx
 * Copyright (C) zml and PermissionsEx contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninja.leaping.permissionsex.bukkit;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.milkbowl.vault.permission.Permission;
import ninja.leaping.permissionsex.data.ImmutableSubjectData;
import ninja.leaping.permissionsex.data.calculated.CalculatedSubject;
import ninja.leaping.permissionsex.exception.PermissionsLoadingException;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static ninja.leaping.permissionsex.PermissionsEx.SUBJECTS_GROUP;
import static ninja.leaping.permissionsex.PermissionsEx.SUBJECTS_USER;

public class PEXVault extends Permission {
    final PermissionsExPlugin plugin;

    public PEXVault(PermissionsExPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return this.plugin.getName();
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.isEnabled();
    }

    @Override
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @Override
    public String[] getGroups() {
        return Iterables.toArray(this.plugin.getGroupSubjects().getAllIdentifiers(), String.class);
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }

    private CalculatedSubject getGroup(String name) {
        try {
            return this.plugin.getManager().getCalculatedSubject(SUBJECTS_GROUP, Preconditions.checkNotNull(name, "name"));
        } catch (PermissionsLoadingException e) {
            throw new RuntimeException(e);
        }
    }

    private CalculatedSubject getSubject(OfflinePlayer player) {
        try {
            return this.plugin.getManager().getCalculatedSubject(SUBJECTS_USER, Preconditions.checkNotNull(player, "player").getUniqueId().toString());
        } catch (PermissionsLoadingException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Map.Entry<String, String>> contextsFrom(@Nullable String world) {
        return world == null ? ImmutableSet.<Map.Entry<String, String>>of() : ImmutableSet.of(Maps.immutableEntry("world", world));
    }


    @Override
    public boolean groupHas(String world, String name, String permission) {
        return getGroup(name).getPermission(contextsFrom(world), permission) > 0;
    }

    @Override
    public boolean groupAdd(final String world, String name, final String permission) {
        return !getGroup(name).update(new Function<ImmutableSubjectData, ImmutableSubjectData>() {
            @Nullable
            @Override
            public ImmutableSubjectData apply(ImmutableSubjectData input) {
                return input.setPermission(contextsFrom(world), permission, 1);
            }
        }).isCancelled();
    }

    @Override
    public boolean groupRemove(final String world, String name, final String permission) {
        return !getGroup(name).update(new Function<ImmutableSubjectData, ImmutableSubjectData>() {
            @Nullable
            @Override
            public ImmutableSubjectData apply(ImmutableSubjectData input) {
                return input.setPermission(contextsFrom(world), permission, 0);
            }
        }).isCancelled();

    }

    @Override
    public boolean playerHas(String world, OfflinePlayer player, String permission) {
        return getSubject(player).getPermission(contextsFrom(world), permission) > 0;
    }

    @Override
    public boolean playerAdd(final String world, OfflinePlayer player, final String permission) {
        return !getSubject(player).update(new Function<ImmutableSubjectData, ImmutableSubjectData>() {
            @Nullable
            @Override
            public ImmutableSubjectData apply(ImmutableSubjectData input) {
                return input.setPermission(contextsFrom(world), permission, 1);
            }
        }).isCancelled();
    }

    @Override
    public boolean playerAddTransient(OfflinePlayer player, String permission) {
        return playerAddTransient(null, player, permission);
    }

    @Override
    public boolean playerAddTransient(Player player, String permission) {
        return playerAddTransient(null, player, permission);
    }

    @Override
    public boolean playerAddTransient(final String worldName, OfflinePlayer player, final String permission) {
        return !getSubject(player).updateTransient(new Function<ImmutableSubjectData, ImmutableSubjectData>() {
            @Nullable
            @Override
            public ImmutableSubjectData apply(ImmutableSubjectData input) {
                return input.setPermission(contextsFrom(worldName), permission, 1);
            }
        }).isCancelled();
    }

    @Override
    public boolean playerRemoveTransient(final String worldName, OfflinePlayer player, final String permission) {
        return !getSubject(player).updateTransient(new Function<ImmutableSubjectData, ImmutableSubjectData>() {
            @Nullable
            @Override
            public ImmutableSubjectData apply(ImmutableSubjectData input) {
                return input.setPermission(contextsFrom(worldName), permission, 0);
            }
        }).isCancelled();
    }

    @Override
    public boolean playerRemove(final String world, OfflinePlayer player, final String permission) {
        return !getSubject(player).update(new Function<ImmutableSubjectData, ImmutableSubjectData>() {
            @Nullable
            @Override
            public ImmutableSubjectData apply(ImmutableSubjectData input) {
                return input.setPermission(contextsFrom(world), permission, 0);
            }
        }).isCancelled();
    }

    @Override
    public boolean playerRemoveTransient(Player player, String permission) {
        return playerRemoveTransient(null, player, permission);
    }

    @Override
    public boolean playerRemoveTransient(OfflinePlayer player, String permission) {
        return playerRemoveTransient(null, player, permission);
    }

    @Override
    public boolean playerInGroup(String world, OfflinePlayer player, String group) {
        return getSubject(player).getParents(contextsFrom(world)).contains(Maps.immutableEntry(SUBJECTS_GROUP, group));
    }

    @Override
    public boolean playerAddGroup(final String world, OfflinePlayer player, final String group) {
        return !getSubject(player).update(new Function<ImmutableSubjectData, ImmutableSubjectData>() {
            @Nullable
            @Override
            public ImmutableSubjectData apply(ImmutableSubjectData input) {
                return input.addParent(contextsFrom(world), SUBJECTS_GROUP, group);
            }
        }).isCancelled();
    }

    @Override
    public boolean playerRemoveGroup(final String world, OfflinePlayer player, final String group) {
        return !getSubject(player).update(new Function<ImmutableSubjectData, ImmutableSubjectData>() {
            @Nullable
            @Override
            public ImmutableSubjectData apply(ImmutableSubjectData input) {
                return input.removeParent(contextsFrom(world), SUBJECTS_GROUP, group);
            }
        }).isCancelled();
    }

    @Override
    public String[] getPlayerGroups(String world, OfflinePlayer player) {
        return FluentIterable.from(getSubject(player).getParents(contextsFrom(world))).filter(new Predicate<Map.Entry<String, String>>() {
            @Override
            public boolean apply(@Nullable Map.Entry<String, String> input) {
                return input.getKey().equals(SUBJECTS_GROUP);
            }
        }).transform(new Function<Map.Entry<String,String>, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Map.Entry<String, String> input) {
                return input.getValue();
            }
        }).toArray(String.class);
    }

    @Override
    public String getPrimaryGroup(String world, OfflinePlayer player) {
        String[] groups = getPlayerGroups(world, player);
        return groups.length > 0 ? groups[0] : null;
    }

    // -- Deprecated methods

    @SuppressWarnings("deprecation")
    private OfflinePlayer pFromName(String name) {
        return this.plugin.getServer().getOfflinePlayer(name);
    }

    @Override
    public boolean playerHas(String world, String name, String permission) {
        return playerHas(world, pFromName(name), permission);
    }

    @Override
    public boolean playerAdd(String world, String name, String permission) {
        return playerAdd(world, pFromName(name), permission);
    }

    @Override
    public boolean playerRemove(String world, String name, String permission) {
        return playerRemove(world, pFromName(name), permission);
    }

    @Override
    public boolean playerInGroup(String world, String player, String group) {
        return playerInGroup(world, pFromName(player), group);
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group) {
        return playerAddGroup(world, pFromName(player), group);
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        return playerRemoveGroup(world, pFromName(player), group);
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        return getPlayerGroups(world, pFromName(player));
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        return getPrimaryGroup(world, pFromName(player));
    }

    @Override
    @Deprecated
    public boolean playerAddTransient(String worldName, String player, String permission) {
        return playerAddTransient(worldName, pFromName(player), permission);
    }

    @Override
    @Deprecated
    public boolean playerRemoveTransient(String worldName, String player, String permission) {
        return playerRemoveTransient(worldName, pFromName(player), permission);
    }
}
