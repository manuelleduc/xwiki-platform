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
(function() {
  'use strict';

  function showImageWizard(editor, widget, isInsert) {

    /**
     * Only called to unwrap the centered images inserted by the old image dialog.
     */
    function unwrapFromCentering(element) {
      var imageOrLink = element.findOne('a,img');

      imageOrLink.replace(element);

      return imageOrLink;
    }


    require(['imageWizard'], function(imageWizard) {
      imageWizard({
        editor: editor,
        imageData: widget.data,
        isInsert: isInsert
      }).done(function(data) {
        if (widget && widget.element) {
          widget.setData(data);

          // With the old image dialog, image were wrapped in a p to be centered. We need to unwrap them to make them
          // compliant with the new image dialog.
          if (widget.element.getName() === 'p') {
            widget.element = unwrapFromCentering(widget.element);
            widget.element.setAttribute('data-widget', widget.name);
          }

        } else {
          var element = CKEDITOR.dom.element.createFromHtml(widget.template.output(), editor.document);
          var wrapper = editor.widgets.wrapElement(element, widget.name);
          var temp = new CKEDITOR.dom.documentFragment(wrapper.getDocument());

          // Append wrapper to a temporary document. This will unify the environment in which #data listeners work when
          // creating and editing widget.
          temp.append(wrapper);

          // Initialize an empty image widget, then update it with the data from the image dialog.
          var widgetInstance = editor.widgets.initOn(element, widget, {});
          widgetInstance.setData(data);
          editor.widgets.finalizeCreation(temp);
        }
      });
    });
  }

  CKEDITOR.plugins.add('xwiki-image', {
    requires: 'xwiki-image-old,xwiki-dialog',
    beforeInit: function(editor) {
      editor.on('widgetDefinition', function(event) {
        var widgetDefinition = event.data;
        if (widgetDefinition.name === "image" && widgetDefinition.dialog === "image2") {
          this.overrideImageWidget(editor, widgetDefinition);
        }
      }, this);
    },
    init: function(editor) {
      this.initImageDialogWidget(editor);
    },
    initImageDialogWidget: function(editor) {
      var imageWidget = editor.widgets.registered.image;
      // this.overrideImageWidget(editor, imageWidget);

      imageWidget.insert = function() {
        showImageWizard(editor, this, true);
      };
      imageWidget.edit = function(event) {
        // Prevent the default behavior because we want to use our custom image dialog.
        event.cancel();
        showImageWizard(editor, this, false);
      };
    },
    overrideImageWidget: function(editor, imageWidget) {
      CKEDITOR.plugins.registered['xwiki-image-old'].overrideImageWidget(editor, imageWidget);

      var originalInit = imageWidget.init;

      function initCentered(widget)
      {
        var resizeWrapper = editor.document.createElement('span');

        resizeWrapper.addClass('cke_image_resizer_wrapper');
        resizeWrapper.append(widget.parts.image);
        // widget.resizer.remove();
        // var resizer = widget.resizer = editor.document.createElement('span');
        // resizer.addClass('cke_image_resizer');
        // resizer.setAttribute('title', editor.lang.image2.resizer);
        // resizer.append(new CKEDITOR.dom.text('\u200b', editor.document));
        resizeWrapper.append(widget.resizer);
        widget.element.append(resizeWrapper, true);
      }

      imageWidget.init = function() {
        // this.inline = false;
        originalInit.call(this);
        
        
        
        
        // var img = this.element;
        // var span = editor.document.createElement('span');
        // span.addClass('cke_widget_element');
        // span.setAttribute('data-widget', 'image');
        //   this.element = span;
        // img.getParent().append(span);
        // img.move(span);

        console.log('GHERE'); // Called first, data second

        // Caption
        if (this.parts.caption) {
          this.setData('hasCaption', true);
          // TODO: Add support for editing the caption directly from the dialog (see CKEDITOR-435)
        } else {
          this.setData('hasCaption', false);
        }

        // Style
        this.setData('imageStyle', this.parts.image.getAttribute('data-xwiki-image-style') || '');

        this.setData('border', this.parts.image.getAttribute('data-xwiki-image-style-border'));
        var alignment = this.parts.image.getAttribute('data-xwiki-image-style-alignment');
        this.setData('alignment', alignment);
        this.setData('textWrap', this.parts.image.getAttribute('data-xwiki-image-style-text-wrap'));

        if (alignment === 'center') {
          initCentered(this);
        }
      };

      var originalData = imageWidget.data;

      function computeStyleData(widget, setAttribute, removeAttribute)
      {
        // Style
        if (widget.data.imageStyle) {
          setAttribute(widget, 'data-xwiki-image-style', widget.data.imageStyle);
        } else {
          removeAttribute(widget, 'data-xwiki-image-style');
        }

        if (widget.data.border) {
          setAttribute(widget, 'data-xwiki-image-style-border', widget.data.border);
        } else {
          removeAttribute(widget, 'data-xwiki-image-style-border');
        }

        // If alignment is undefined, try to convert from the legacy align data property.
        var mapping = {left: 'start', right: 'end', center: 'center'};
        widget.data.alignment = widget.data.alignment || mapping[widget.data.align] || 'none';

        // The old align needs to be undefined otherwise it's not removed when re-inserting the image after the edition,
        // add deprecated attributes to the image.
        widget.data.align = 'none';

        if (widget.data.alignment && widget.data.alignment !== 'none') {
          setAttribute(widget, 'data-xwiki-image-style-alignment', widget.data.alignment);
        } else {
          removeAttribute(widget, 'data-xwiki-image-style-alignment');
        }

        if (widget.data.textWrap) {
          setAttribute(widget, 'data-xwiki-image-style-text-wrap', widget.data.textWrap);
        } else {
          removeAttribute(widget, 'data-xwiki-image-style-text-wrap');
        }
      }

      imageWidget.data = function() {

        /**
         * Update the given attribute at two locations in the widget, the image tag and the widget itself.
         *
         * @param widget the widget to update
         * @param key the attribute key
         * @param value the attribute value
         */
        function setAttribute(widget, key, value) {
          widget.parts.image.setAttribute(key, value);
          widget.wrapper.setAttribute(key, value);
        }

        /**
         * Remove the given attribute at two locations on the widget, the image tag and the widget itself.
         *
         * @param widget the widget to update
         * @param key the property key to removew
         */
        function removeAttribute(widget, key) {
          widget.parts.image.removeAttribute(key);
          widget.wrapper.removeAttribute(key);
        }

        // Note: must be computed early.
        var data = this.data || {};
        var alignmentChanged = this.oldData && data.alignment !== this.oldData.alignment;

        // Caption
        // TODO: Add support for editing the caption directly from the dialog (see CKEDITOR-435)

        computeStyleData(this, setAttribute, removeAttribute);

        originalData.call(this);

        
        if(alignmentChanged) {
          var newWidget = editor.widgets.initOn(this.element, 'image', this.data);
          console.log('newWidget', newWidget);
          newWidget.focus();
        }
      };

      var originalUpcast = imageWidget.upcast;
      // @param {CKEDITOR.htmlParser.element} element
      // @param {Object} data
      imageWidget.upcast = function (element, data) {
        console.log('upcast start');
        var imgUpcasted = originalUpcast.apply(this, arguments);
        console.log('upcast end', imgUpcasted);
        var el = imgUpcasted;
        if (imgUpcasted) {
          var img = imgUpcasted;
          var span = new CKEDITOR.htmlParser.element( 'span' );
          // span.addClass('cke_widget_element');
          // span.setAttribute('data-widget', 'image');
          //   this.element = span;
          // img.getParent().append(span);
          img.wrapWith(span);
          el = span;
        }
        return el;

      };


      var originalDowncast = imageWidget.downcast;
      imageWidget.downcast = function(element) {
        console.log('downcast start');
        var apply = originalDowncast.apply(this, arguments);
        console.log('downcast end', apply);
        return apply;
      };
      
      // var originalUpcast = imageWidget.upcast;
      // imageWidget.upcast = function (editor) {
      //   return function (el, data) {
      //     console.log('upcast');
      //     var internalUpcast = originalUpcast.call(this, editor);
      //     return internalUpcast.call(this, el, data);
      //   }.bind(this);
      // };
      //
      // var originalDowncast = imageWidget.downcast;
      // imageWidget.downcast = function (editor) {
      //  
      //   return function (el) {
      //     console.log('downcast');
      //     var internalDowncast = originalDowncast.call(this, editor);
      //     return internalDowncast.call(this, el);
      //   }.bind(this);
      // };
    }
  });

})();
