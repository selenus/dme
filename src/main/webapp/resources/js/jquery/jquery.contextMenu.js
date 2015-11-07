/**
 * nuContextMenu - jQuery Plugin
 * Copyright (c) 2015, Alex Suyun
 * Copyrights licensed under The MIT License (MIT)
 */
;
(function($, window, document, undefined) {

    'use strict';

    var plugin = 'nuContextMenu';

    var defaults = {
        contextMenuClass: 'nu-context-menu',
        activeClass: 'active',
    };

    var nuContextMenu = function(container, options) {
        this.container = $(container);
        this.options = $.extend({}, defaults, options);
        this._defaults = defaults;
        this._name = plugin;
        this.init();
    };

    $.extend(nuContextMenu.prototype, {
        init: function() {

            if (this.options.items) {
                this.items = $(this.options.items);
            }
            
            if (this._buildContextMenu()) {
                this._bindEvents();
                this._menuVisible = this._menu===undefined ? false : this._menu.hasClass(this.options.activeClass);
            }
        },

        _getCallback: function() {
            return ((this.options.callback && typeof this.options.callback === 'function') ? this.options.callback : function () {});
        },

        _buildContextMenu: function() {
            if (this.options.menu===undefined || (typeof this.options.menu !== 'object' && typeof this.options.menu !== 'function')) {
                return false;
            } 
            
            
       	 	// Create context menu
            this._menu = $('<div>')
                .addClass(this.options.contextMenuClass)
                .append('<ul>');
            
            $('body').append(this._menu);
            
            if (typeof this.options.menu === 'object') {
            	this._buildMenuItems(this.options.menu);
            }
            return true;

        },
        
        _buildMenuItems: function(menu) {
        	
        	this._menu.children('ul').empty();

            var menuObject = menu,
                menuList = this._menu.children('ul');

            // Create menu items 
            $.each(menuObject, function(key, value) {
                var item;
                if (value === '---------') {
                    item = $('<hr>');
                } else if (value && typeof value === 'object') {

                    item = $('<li>')
                    	.attr('data-key', key)
                    	.attr('data-id', value.id)
                    	.attr('data-item', value.type)
                    	.text(' ' + value.title);

                    // Font-awesome support
                    if (value.icon) {
                    	if (typeof value.icon === 'function') {
                    		var icon = value.icon(key, value);
                    	} else {
	                        var icon = $('<i>').addClass('fa fa-' + value.icon.toString());
                    	}
                    	item.prepend(icon);
                    }

                }

                menuList.append(item);

            });

            
        	
        },

        _pDefault: function(event) {
            event.preventDefault();
            event.stopPropagation();
            return false;
        },

        _contextMenu: function(event) {
            if (this.options.menu != undefined && typeof this.options.menu == 'function') {
            	var menu = this.options.menu(event);
            	if (menu===undefined || menu===null || menu.length==0) {
            		return false;
            	}
            	this._buildMenuItems(menu);
            }
            
            event.preventDefault();

            var element = event.target;
            
            if (this._menuVisible) {
                return false;
            }

            if (this.options.disable) {
                return false;
            }

            // Callback function that will be attached
            // to the list items 
            var callback = this._getCallback();

            var listItems = this._menu.children('ul').children('li');
            // Prevent memory leak 
            listItems.off();

            var _this = this;
            listItems.on('click', function() {
                var key = $(this).attr('data-key');
                var id = $(this).attr('data-id');
                var type = $(this).attr('data-item');
                callback(element, key, id, type);
                _this._close();
            });

            this._menu.addClass(this.options.activeClass);
            this._menuVisible = true;
            this._menu.css({
                'top': event.pageY + 'px',
                'left': event.pageX + 'px'
            });

            return true;
        },

        _onMouseDown: function(event) {
            // Remove menu if clicked outside
            if (!$(event.target).parents('.' + this.options.contextMenuClass).length) {
                this._close();
            }
        },
        _close: function() {
        	this._menu.removeClass(this.options.activeClass);
            this._menuVisible = false;
            if (this.options.menu != undefined && typeof this.options.menu == 'function') {
            	this._menu.children('ul').empty();
            }
        },

        _bindEvents: function() {

            if (this.items) {
                this.items.on('contextmenu', $.proxy(this._contextMenu, this));
                this.container.on('contextmenu', $.proxy(this._pDefault, this));
            } else {
                this.container.on('contextmenu', $.proxy(this._contextMenu, this));
            }

            // Remove menu on click 
            $(document).on('mousedown', $.proxy(this._onMouseDown, this));

        },

        disable: function() {
            this.options.disable = true;
            return true;
        },

    });

    $.fn[plugin] = function(options) {
        var args = Array.prototype.slice.call(arguments, 1);

        return this.each(function() {
            var item = $(this),
                instance = item.data(plugin);
            if (!instance) {
                item.data(plugin, new nuContextMenu(this, options));
            } else {

                if (typeof options === 'string' && options[0] !== '_' && options !== 'init') {
                    instance[options].apply(instance, args);
                }
            }
        });
    };

})(jQuery, window, document);
