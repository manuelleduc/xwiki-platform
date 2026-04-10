<!--
  See the NOTICE file distributed with this work for additional
  information regarding copyright ownership.

  This is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2.1 of
  the License, or (at your option) any later version.

  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this software; if not, write to the Free
  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
-->

<!--
  The GuidedTourWidget

  It contains a single default slot.
-->

<template>
  {{ cptr }} <button @click="cptr++">ADD</button>
  <div class="guidedtour-widget" :class="widgetClass">
    <GuidedTourWidgetHeader
      @collapseGuidedTourWidget="onToggleCollapseTour"
      :progress="progress"
    />
    <div class="guidedtour-widget-content">
      <div class="guidedtour-container">
        <!-- FIXME: There should be a better grouping style here, groups shouldn't be sections. -->
        <template v-if="state.tours.length">
          <GuidedTourWidgetTour
            v-for="tour in state.tours"
            :key="tour.id"
            :tour="reactive(tour)"
            @toggleCollapseTour="
              (tour: TourTour) => {
                console.debug('toggleCollapseTour closeset for ', tour);
                tour.isCollapsed = !tour.isCollapsed;
              }
            "
          />
        </template>
        <div v-else>No tours</div>
      </div>
      <div>
        <template v-if="state.usefulLinks.length">
          <GuidedTourWidgetUsefulLink
            v-for="(link, index) in state.usefulLinks"
            :key="index"
            :link="link"
          />
        </template>
        <div v-else>No links</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
//import type { I18n } from "vue-i18n";
// All the logic should live here (TODO: Maybe move most of it to a .ts file)
// FIXME: This should be injected from somewhere else, but I have no idea from where.
import GuidedTourWidgetHeader from "./GuidedTourWidgetHeader.vue";
import GuidedTourWidgetTour from "./GuidedTourWidgetTour.vue";
import GuidedTourWidgetUsefulLink from "./GuidedTourWidgetUsefulLink.vue";
import { TourTaskStatus } from "@xwiki/platform-guidedtour-api";
import { computed, onMounted, provide, reactive, ref } from "vue";
import type {
  GuidedTourManagerApi,
  TourTour,
} from "@xwiki/platform-guidedtour-api";
const cptr = ref(0);
console.info("In widget setup. 23");
const { guidedTourManager } = defineProps<{
  guidedTourManager: GuidedTourManagerApi;
}>();

provide<GuidedTourManagerApi>("GuidedTourManager", guidedTourManager!);

// onErrorCaptured((err) => {
//   console.error(err);
// });
const state = reactive({
  isWidgetCollapsed: true,
  tours: [] as TourTour[],
  usefulLinks: [] as string[],
  isWidgetShown: true,
});
const widgetClass = computed(() => ({ collapsed: state.isWidgetCollapsed }));

onMounted(async () => {
  state.tours = await guidedTourManager.getTours();
  state.usefulLinks = await guidedTourManager.getUsefulLinks();
  state.isWidgetShown = await guidedTourManager.isWidgetShown();
  console.log("async loaded", state.tours);
});

function onToggleCollapseTour() {
  state.isWidgetCollapsed = !state.isWidgetCollapsed;
  console.debug("collapseGuidedTourWidget", state.isWidgetCollapsed);
}

console.log(
  "loaded",
  state.tours,
  state.usefulLinks,
  state.isWidgetShown,
  guidedTourManager,
);
let progress = ref(0.3);
progress.value +=
  0.3 +
  state.tours.filter((tour: TourTour) => tour.status != TourTaskStatus.ToDo)
    .length /
    state.tours.length;
// FIXME
// Fetch the tour from the API
// Instantiate the JS objects to be used in the widget
/*
define('guidedtour-widget', ['jquery', 'guidedtour-utils'], function($, utils) {
  const guidedTourFloaterTemplate = `bababooey`;

  // Create widget. Bind Events.
  $(guidedTourFloaterTemplate).appendTo($(document.body));

  // Helper to bind click events, TODO: could be deleted.
  function bindFloaterClickEvent(selector, callback) {
    $('.guidedtour-widget ' + selector).on('click', (event) =&gt; {
      callback(event);
    });
  };

  bindFloaterClickEvent('.top-bar', (event) =&gt; {
    window.localStorage.setItem('TourFloaterCollapsed', document.querySelector('.guidedtour-widget').classList.toggle('collapsed'));
  });

  bindFloaterClickEvent('#widget-close', (event) =&gt; {
    if (event.target.closest('.guidedtour-widget').classList.contains('collapsed')) {
      event.target.closest('.guidedtour-widget').remove();
      // window.localStorage.setItem('TourFloaterCollapsed', 'hidden') // Commented to not disable the widget completely, permanently.
      event.stopPropagation();
      // TODO: Or just hide it, and set some cookie/option to not load the javascript code at all the next time.
    }
  });

  bindFloaterClickEvent('#widget-options', (event) =&gt; {
    event.stopPropagation();
    console.info('Opened settings menu');
  });

  // Load localStorage state.
  if (window.localStorage.getItem('TourFloaterCollapsed') == 'true') {
    document.querySelector('.guidedtour-widget').classList.add('collapsed');
  }
  if (window.localStorage.getItem('guidedtour-widget-position-x')) {
    // FIXME: Could be XSS i think, if someone edits this key. But that's how the right side panel works too.
    // FIXME: Clamp the allowed values, so the widget is always visible on the screen. Maybe set the value as percentage of screen width? In the dragging functions I mean.
    document.querySelector('.guidedtour-widget').style.left = window.localStorage.getItem('guidedtour-widget-position-x');
  }

  // Definitions.
  /*
   * Function to set up a draggable element, for the widget.
   * Taken from https://www.w3schools.com/howto/howto_js_draggable.asp
   *\/
  function dragElement(elmnt) {
    console.debug(elmnt)
    if (!elmnt.classList.contains('draggable')) {
      return;
    }
    var pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
    // otherwise, move the DIV from anywhere inside the DIV:
    elmnt.onmousedown = dragMouseDown;

    function dragMouseDown(e) {
      e = e || window.event;
      e.preventDefault();
      e.stopPropagation();
      e.stopImmediatePropagation();
      // get the mouse cursor position at startup:
      pos3 = e.clientX;
      pos4 = e.clientY;
      document.onmouseup = closeDragElement;
      // call a function whenever the cursor moves:
      document.onmousemove = elementDrag;
    }

    function elementDrag(e) {
      e = e || window.event;
      e.preventDefault();
      e.stopPropagation();
      e.stopImmediatePropagation();
      document.body.style.setProperty('cursor', 'grabbing', 'important');
      elmnt.classList.add('dragging');
      // calculate the new cursor position:
      pos1 = pos3 - e.clientX;
      pos2 = pos4 - e.clientY;
      pos3 = e.clientX;
      pos4 = e.clientY;
      // set the element's new position:
      //elmnt.style.top = (elmnt.offsetTop - pos2) + "px"; // Commented so the drag only goes side-to-side, not up-down.
      // TODO: Make sure the widget doesn't end up outside the window post-window-resize.
      elmnt.style.left = (elmnt.offsetLeft - pos1) + "px";
    }

    function closeDragElement(e) {
      // Stop moving when mouse button is released:
      e.preventDefault();
      e.stopPropagation();
      e.stopImmediatePropagation();
      document.onmouseup = null;
      document.onmousemove = null;
      document.body.style.cursor = "";
      elmnt.classList.remove('dragging');
      window.localStorage.setItem('guidedtour-widget-position-x', elmnt.style.left);
    }
  }

  utils.getTourData().then((data) =&gt; {
    console.info(data);
    const groups = data['groups'];
    // TODO: Maybe re-enable this and improve the functionality.
    // dragElement(document.querySelector('.draggable'));
    for (group in groups) {
      let groupEl = document.createElement('section');
      groupEl.setAttribute('data-gid', group);
      groupEl.classList.add('guidedtour-tour');
      if (window.localStorage.getItem('tour_95_' + utils.escapeTourName(groupEl['dataset'].gid) + '_TourFloaterTourCollapsed') == 'true') {
        groupEl.classList.add('collapsed');
      }
      console.debug(groups[group])
      // FIXME: Add HTML escapes (maybe use purify?)
      groupEl.innerHTML = '&lt;div class="guidedtour-tour-header"&gt;&lt;i class="fa-solid fa-chevron-right chevron"&gt;&lt;/i&gt;' + groups[group]['displayTitle'] + '&lt;/div&gt;&lt;div class="guidedtour-content"&gt;&lt;/div&gt;';
      // Chevron collapse section.
      groupEl.getElementsByClassName('guidedtour-tour-header')[0].onclick = (event) =&gt; {
        const contentElement = event.target.closest('.guidedtour-tour');
        window.localStorage.setItem('tour_95_' + utils.escapeTourName(contentElement['dataset'].gid) + '_TourFloaterTourCollapsed', contentElement.classList.toggle('collapsed'));
      };
      // TODO: Replace divs with actual ARIA/WCAG stuff, ul, etc
      groups[group]['tasks'].forEach(task =&gt; {
        if (!task.raw.isActive || task.raw.isHidden) {
          return;
        }
        let taskEl = document.createElement('div');
        taskEl.innerHTML = '&lt;button class="pre-btn"&gt;&lt;i class="fa fa-arrow-right"&gt;&lt;/i&gt;&lt;/button&gt;' + task['title'] + '&lt;button class="post-btn"&gt;&lt;i class="fa fa-rotate-right"&gt;&lt;/i&gt;&lt;/button&gt;';
        taskEl.setAttribute('data-id', task['id']);
        taskEl.className = 'guidedtour-task';
        if (task['done']) {
          taskEl.classList.add('task-done')
        }
        groupEl.querySelector('.guidedtour-content').appendChild(taskEl);
      });
      if (groupEl.querySelector('.guidedtour-content').childElementCount &gt; 0) {
        document.querySelector('.guidedtour-container').appendChild(groupEl);
      }
    }
  });
});
*/
</script>

<style>
.guidedtour-widget.dragging * {
  pointer-events: none;
}

.guidedtour-widget.collapsed .guidedtour-widget-content {
  width: 0;
  max-height: 0;
}

.guidedtour-widget {
  z-index: 999;
  position: fixed;
  bottom: 0px;
  right: 0px;
  box-shadow: 0px 0px 12px 0px #00000033;
  background-color: white;
  width: fit-content;
  overflow: hidden;
  display: inline-block;
  border-start-start-radius: 8px;
  user-select: none;

  .guidedtour-widget-content {
    overflow: hidden;
  }

  .guidedtour-content {
    display: flex;
    max-height: 400px; /* Placeholder so the height animates nicely. */
    flex-direction: column;
    overflow: hidden;
  }

  .collapsed .guidedtour-content {
    max-height: 0px;
  }

  .guidedtour-container {
    overflow-x: scroll;
    padding: 0px 16px 14px 16px;
    max-height: 300px;
  }

  * {
    transition: max-height 0.45s cubic-bezier(0.25, 1, 0.25, 1);
    /*, width 0.45s ease-in-out 0s*/
  }
}
</style>
