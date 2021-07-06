<!--
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
 -->


<!--
  The LivedataBottombar component can be use to create consistent topbars
  for the different layout implementations.
  It usually contains the dropdown menu, the refresh button, the global search
  and the pagination.

  It contains two slots: `left` and `right`, that allows to quickly
  organize its content to stick to the left or to the right
-->
<template>
  <div class="livedata-bottombar">
    <slot></slot>
  </div>
</template>


<script>
export default {
  name: "LivedataBottombar",
};
</script>


<style>
.livedata-bottombar {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}
</style>
