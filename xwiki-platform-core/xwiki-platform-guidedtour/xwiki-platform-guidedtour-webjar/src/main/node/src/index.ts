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

import {GuidedTourManager} from '@xwiki/platform-guidedtour-xwiki';
import {GuidedTourWidget} from "@xwiki/platform-guidedtour-ui"
import {Suspense, createApp, h} from 'vue';


function init() {
  console.info("Hi from init 1!");
  const guidedTourManager = new GuidedTourManager();

  // createApp({ render: () => h('div', 'Test') }).mount('#gt2');
  const app = createApp(GuidedTourWidget, { guidedTourManager });
  app.config.errorHandler = (err, instance, info) => {
    console.error('Vue error:', err, info, instance);
    throw err;
  };
  app.config.warnHandler = (msg, instance, trace) => {
    console.warn('Vue warn:', msg, trace, instance);
  };

  // app.config.devtools = true;
  // app.config.productionTip = false;

  const a = app.mount('#guidedtour-uix');
  console.log(a);
}

if (!document.querySelector('#tourResumeContainer')) {
  init();
} else {
  console.warn("Since the Tour Application is installed, not showing the GuidedTour Widget, to prevent conflicts.");
  init(); // TODO: Remove this, this is temporary for testing.
}