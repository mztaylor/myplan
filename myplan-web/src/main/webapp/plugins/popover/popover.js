(function (a) {
    a.fn.IsPopOverOpen = function () {
        var c = null;
        a(this).each(function (d, e) {
            var b = a(e).data("private_jquerypopover_options");
            if (b != null && typeof b == "object" && !a.isArray(b) && !a.isEmptyObject(b) && b.privateVars != null && typeof b.privateVars == "object" && !a.isArray(b.privateVars) && !a.isEmptyObject(b.privateVars) && typeof b.privateVars.is_open != "undefined") {
                c = b.privateVars.is_open ? true : false
            }
            return false
        });
        return c
    };
    a.fn.GetPopOverLastDisplayDateTime = function () {
        var b = null;
        a(this).each(function (e, f) {
            var d = a(f).data("private_jquerypopover_options");
            if (d != null && typeof d == "object" && !a.isArray(d) && !a.isEmptyObject(d) && d.privateVars != null && typeof d.privateVars == "object" && !a.isArray(d.privateVars) && !a.isEmptyObject(d.privateVars) && typeof d.privateVars.last_display_datetime != "undefined" && d.privateVars.last_display_datetime != null) {
                b = c(d.privateVars.last_display_datetime)
            }
            return false
        });

        function c(d) {
            return new Date(d * 1000)
        }

        return b
    };
    a.fn.GetPopOverLastModifiedDateTime = function () {
        var b = null;
        a(this).each(function (e, f) {
            var d = a(f).data("private_jquerypopover_options");
            if (d != null && typeof d == "object" && !a.isArray(d) && !a.isEmptyObject(d) && d.privateVars != null && typeof d.privateVars == "object" && !a.isArray(d.privateVars) && !a.isEmptyObject(d.privateVars) && typeof d.privateVars.last_modified_datetime != "undefined" && d.privateVars.last_modified_datetime != null) {
                b = c(d.privateVars.last_modified_datetime)
            }
            return false
        });

        function c(d) {
            return new Date(d * 1000)
        }

        return b
    };
    a.fn.GetPopOverCreationDateTime = function () {
        var b = null;
        a(this).each(function (e, f) {
            var d = a(f).data("private_jquerypopover_options");
            if (d != null && typeof d == "object" && !a.isArray(d) && !a.isEmptyObject(d) && d.privateVars != null && typeof d.privateVars == "object" && !a.isArray(d.privateVars) && !a.isEmptyObject(d.privateVars) && typeof d.privateVars.creation_datetime != "undefined" && d.privateVars.creation_datetime != null) {
                b = c(d.privateVars.creation_datetime)
            }
            return false
        });

        function c(d) {
            return new Date(d * 1000)
        }

        return b
    };
    a.fn.GetPopOverMarkup = function () {
        var b = null;
        a(this).each(function (d, e) {
            var c = a(e).data("private_jquerypopover_options");
            if (c != null && typeof c == "object" && !a.isArray(c) && !a.isEmptyObject(c) && c.privateVars != null && typeof c.privateVars == "object" && !a.isArray(c.privateVars) && !a.isEmptyObject(c.privateVars) && typeof c.privateVars.id != "undefined") {
                b = a("#" + c.privateVars.id).length > 0 ? a("#" + c.privateVars.id).html() : null
            }
            return false
        });
        return b
    };
    a.fn.GetPopOverID = function () {
        var b = null;
        a(this).each(function (d, e) {
            var c = a(e).data("private_jquerypopover_options");
            if (c != null && typeof c == "object" && !a.isArray(c) && !a.isEmptyObject(c) && c.privateVars != null && typeof c.privateVars == "object" && !a.isArray(c.privateVars) && !a.isEmptyObject(c.privateVars) && typeof c.privateVars.id != "undefined") {
                b = c.privateVars.id
            }
            return false
        });
        return b
    };
    a.fn.RemovePopOver = function () {
        var b = 0;
        a(this).each(function (d, e) {
            var c = a(e).data("private_jquerypopover_options");
            if (c != null && typeof c == "object" && !a.isArray(c) && !a.isEmptyObject(c) && c.privateVars != null && typeof c.privateVars == "object" && !a.isArray(c.privateVars) && !a.isEmptyObject(c.privateVars) && typeof c.privateVars.id != "undefined") {
                a(e).unbind("managepopover");
                a(e).unbind("setpopoverinnerhtml");
                a(e).unbind("setpopoveroptions");
                a(e).unbind("positionpopover");
                a(e).unbind("freezepopover");
                a(e).unbind("unfreezepopover");
                a(e).unbind("showpopover");
                a(e).unbind("hidepopover");
                a(e).data("private_jquerypopover_options", {});
                if (a("#" + c.privateVars.id).length > 0) {
                    a("#" + c.privateVars.id).remove()
                }
                b++
            }
        });
        return b
    };
    a.fn.HasPopOver = function () {
        var c = false;
        a(this).each(function (d, e) {
            var b = a(e).data("private_jquerypopover_options");
            if (b != null && typeof b == "object" && !a.isArray(b) && !a.isEmptyObject(b) && b.privateVars != null && typeof b.privateVars == "object" && !a.isArray(b.privateVars) && !a.isEmptyObject(b.privateVars) && typeof b.privateVars.id != "undefined") {
                c = true
            }
            return false
        });
        return c
    };
    a.fn.GetPopOverOptions = function () {
        var b = {};
        a(this).each(function (c, d) {
            b = a(d).data("private_jquerypopover_options");
            if (b != null && typeof b == "object" && !a.isArray(b) && !a.isEmptyObject(b) && b.privateVars != null && typeof b.privateVars == "object" && !a.isArray(b.privateVars) && !a.isEmptyObject(b.privateVars)) {
                delete b.privateVars
            } else {
                b = null
            }
            return false
        });
        if (a.isEmptyObject(b)) {
            b = null
        }
        return b
    };
    a.fn.SetPopOverInnerHtml = function (b, c) {
        a(this).each(function (d, e) {
            if (typeof c != "boolean") {
                c = true
            }
            a(e).trigger("setpopoverinnerhtml", [b, c])
        })
    };
    a.fn.SetPopOverOptions = function (b) {
        a(this).each(function (c, d) {
            a(d).trigger("setpopoveroptions", [b])
        })
    };
    a.fn.ShowPopOver = function (b, c) {
        a(this).each(function (d, e) {
            a(e).trigger("showpopover", [b, c, true]);
            return false
        })
    };
    a.fn.ShowAllPopOvers = function (b, c) {
        a(this).each(function (d, e) {
            a(e).trigger("showpopover", [b, c, true])
        })
    };
    a.fn.HidePopOver = function () {
        a(this).each(function (b, c) {
            a(c).trigger("hidepopover", [true]);
            return false
        })
    };
    a.fn.HideAllPopOvers = function () {
        a(this).each(function (b, c) {
            a(c).trigger("hidepopover", [true])
        })
    };
    a.fn.FreezePopOver = function () {
        a(this).each(function (b, c) {
            a(c).trigger("freezepopover");
            return false
        })
    };
    a.fn.FreezeAllPopOvers = function () {
        a(this).each(function (b, c) {
            a(c).trigger("freezepopover")
        })
    };
    a.fn.UnfreezePopOver = function () {
        a(this).each(function (b, c) {
            a(c).trigger("unfreezepopover");
            return false
        })
    };
    a.fn.UnfreezeAllPopOvers = function () {
        a(this).each(function (b, c) {
            a(c).trigger("unfreezepopover")
        })
    };
    a.fn.CreatePopOver = function (e) {
        var r = {
            me:this,
            cache:[],
            options_key:"private_jquerypopover_options",
            model_tr:["top", "middle", "bottom"],
            model_td:["left", "middle", "right"],
            model_markup:'<div role="dialog" tabindex="-1" class="{BASE_CLASS} {TEMPLATE_CLASS}"{DIV_STYLE} id="{DIV_ID}"><table{TABLE_STYLE}><tbody><tr><td class="{BASE_CLASS}-top-left"{TOP-LEFT_STYLE}>{TOP-LEFT}</td><td class="{BASE_CLASS}-top-middle"{TOP-MIDDLE_STYLE}>{TOP-MIDDLE}</td><td class="{BASE_CLASS}-top-right"{TOP-RIGHT_STYLE}>{TOP-RIGHT}</td></tr><tr><td class="{BASE_CLASS}-middle-left"{MIDDLE-LEFT_STYLE}>{MIDDLE-LEFT}</td><td class="{BASE_CLASS}-innerHtml"{INNERHTML_STYLE}>{INNERHTML}</td><td class="{BASE_CLASS}-middle-right"{MIDDLE-RIGHT_STYLE}>{MIDDLE-RIGHT}</td></tr><tr><td class="{BASE_CLASS}-bottom-left"{BOTTOM-LEFT_STYLE}>{BOTTOM-LEFT}</td><td class="{BASE_CLASS}-bottom-middle"{BOTTOM-MIDDLE_STYLE}>{BOTTOM-MIDDLE}</td><td class="{BASE_CLASS}-bottom-right"{BOTTOM-RIGHT_STYLE}>{BOTTOM-RIGHT}</td></tr></tbody></table></div>',
            privateVars:{
                id:null,
                creation_datetime:null,
                last_modified_datetime:null,
                last_display_datetime:null,
                is_open:false,
                is_freezed:false,
                is_animating:false,
                is_animation_complete:false,
                is_mouse_over:false,
                is_position_changed:false,
                last_options:{}
            },
            position:"top",
            positionValues:["left", "top", "right", "bottom"],
            align:"center",
            alignValues:["left", "center", "right", "top", "middle", "bottom"],
            alignHorizontalValues:["left", "center", "right"],
            alignVerticalValues:["top", "middle", "bottom"],
            distance:"0px",
            width:null,
            height:null,
            divStyle:{},
            tableStyle:{},
            innerHtml:null,
            innerHtmlStyle:{},
            tail:{
                align:"center",
                hidden:false
            },
            dropShadow:true,
            alwaysVisible:true,
            selectable:true,
            manageMouseEvents:true,
            mouseMove:"show",
            mouseOverValues:["show", "hide"],
            mouseOut:"hide",
            mouseOutValues:["show", "hide"],
            openingSpeed:0,
            closingSpeed:0,
            openingDelay:0,
            closingDelay:0,
            baseClass:"jquerypopover",
            themeName:"myplan",
            themePath:"jquerypopover-theme/",
            themeMargins:{
                total:"13px",
                difference:"10px"
            },
            afterShown:function () {
            },
            afterHidden:function () {
            },
            hideElementId:[]
        };
        h(e);

        function g(v) {
            var w = {
                privateVars:{},
                width:r.width,
                height:r.height,
                divStyle:r.divStyle,
                tableStyle:r.tableStyle,
                position:r.position,
                align:r.align,
                distance:r.distance,
                openingSpeed:r.openingSpeed,
                closingSpeed:r.closingSpeed,
                openingDelay:r.openingDelay,
                closingDelay:r.closingDelay,
                mouseMove:r.mouseMove,
                mouseOut:r.mouseOut,
                tail:r.tail,
                innerHtml:r.innerHtml,
                innerHtmlStyle:r.innerHtmlStyle,
                baseClass:r.baseClass,
                themeName:r.themeName,
                themePath:r.themePath,
                themeMargins:r.themeMargins,
                dropShadow:r.dropShadow,
                manageMouseEvents:r.manageMouseEvents,
                alwaysVisible:r.alwaysVisible,
                selectable:r.selectable,
                afterShown:r.afterShown,
                afterHidden:r.afterHidden,
                hideElementId:r.hideElementId
            };
            var t = a.extend(false, w, (typeof v == "object" && !a.isArray(v) && !a.isEmptyObject(v) && v != null ? v : {}));
            t.privateVars.id = r.privateVars.id;
            t.privateVars.creation_datetime = r.privateVars.creation_datetime;
            t.privateVars.last_modified_datetime = r.privateVars.last_modified_datetime;
            t.privateVars.last_display_datetime = r.privateVars.last_display_datetime;
            t.privateVars.is_open = r.privateVars.is_open;
            t.privateVars.is_freezed = r.privateVars.is_freezed;
            t.privateVars.is_animating = r.privateVars.is_animating;
            t.privateVars.is_animation_complete = r.privateVars.is_animation_complete;
            t.privateVars.is_mouse_over = r.privateVars.is_mouse_over;
            t.privateVars.is_position_changed = r.privateVars.is_position_changed;
            t.privateVars.last_options = r.privateVars.last_options;
            t.width = (typeof t.width == "string" || typeof t.width == "number") && parseInt(t.width) > 0 ? parseInt(t.width) : r.width;
            t.height = (typeof t.height == "string" || typeof t.height == "number") && parseInt(t.height) > 0 ? parseInt(t.height) : r.height;
            t.divStyle = t.divStyle != null && typeof t.divStyle == "object" && !a.isArray(t.divStyle) && !a.isEmptyObject(t.divStyle) ? t.divStyle : r.divStyle;
            t.tableStyle = t.tableStyle != null && typeof t.tableStyle == "object" && !a.isArray(t.tableStyle) && !a.isEmptyObject(t.tableStyle) ? t.tableStyle : r.tableStyle;
            t.position = typeof t.position == "string" && o(t.position.toLowerCase(), r.positionValues) ? t.position.toLowerCase() : r.position;
            t.align = typeof t.align == "string" && o(t.align.toLowerCase(), r.alignValues) ? t.align.toLowerCase() : r.align;
            t.distance = (typeof t.distance == "string" || typeof t.distance == "number") && parseInt(t.distance) >= 0 ? parseInt(t.distance) : r.distance;
            t.openingSpeed = typeof t.openingSpeed == "number" && parseInt(t.openingSpeed) > 0 ? parseInt(t.openingSpeed) : r.openingSpeed;
            t.closingSpeed = typeof t.closingSpeed == "number" && parseInt(t.closingSpeed) > 0 ? parseInt(t.closingSpeed) : r.closingSpeed;
            t.openingDelay = typeof t.openingDelay == "number" && t.openingDelay >= 0 ? t.openingDelay : r.openingDelay;
            t.closingDelay = typeof t.closingDelay == "number" && t.closingDelay >= 0 ? t.closingDelay : r.closingDelay;
            t.mouseMove = typeof t.mouseMove == "string" && o(t.mouseMove.toLowerCase(), r.mouseOverValues) ? t.mouseMove.toLowerCase() : r.mouseMove;
            t.mouseOut = typeof t.mouseOut == "string" && o(t.mouseOut.toLowerCase(), r.mouseOutValues) ? t.mouseOut.toLowerCase() : r.mouseOut;
            t.tail = t.tail != null && typeof t.tail == "object" && !a.isArray(t.tail) && !a.isEmptyObject(t.tail) ? t.tail : r.tail;
            t.tail.align = typeof t.tail.align != "undefined" ? t.tail.align : r.tail.align;
            t.tail.hidden = typeof t.tail.hidden != "undefined" ? t.tail.hidden : r.tail.hidden;
            t.innerHtml = typeof t.innerHtml == "string" && t.innerHtml.length > 0 ? t.innerHtml : r.innerHtml;
            t.innerHtmlStyle = t.innerHtmlStyle != null && typeof t.innerHtmlStyle == "object" && !a.isArray(t.innerHtmlStyle) && !a.isEmptyObject(t.innerHtmlStyle) ? t.innerHtmlStyle : r.innerHtmlStyle;
            t.baseClass = j(typeof t.baseClass == "string" && t.baseClass.length > 0 ? t.baseClass : r.baseClass);
            t.themeName = typeof t.themeName == "string" && t.themeName.length > 0 ? a.trim(t.themeName) : r.themeName;
            t.themePath = typeof t.themePath == "string" && t.themePath.length > 0 ? a.trim(t.themePath) : r.themePath;
            t.themeMargins = t.themeMargins != null && typeof t.themeMargins == "object" && !a.isArray(t.themeMargins) && !a.isEmptyObject(t.themeMargins) && (typeof parseInt(t.themeMargins.total) == "number" && typeof parseInt(t.themeMargins.difference) == "number") ? t.themeMargins : r.themeMargins;
            t.dropShadow = typeof t.dropShadow == "boolean" && t.dropShadow == true ? true : false;
            t.manageMouseEvents = typeof t.manageMouseEvents == "boolean" && t.manageMouseEvents == true ? true : false;
            t.alwaysVisible = typeof t.alwaysVisible == "boolean" && t.alwaysVisible == true ? true : false;
            t.selectable = typeof t.selectable == "boolean" && t.selectable == true ? true : false;
            t.afterShown = typeof t.afterShown == "function" ? t.afterShown : r.afterShown;
            t.afterHidden = typeof t.afterHidden == "function" ? t.afterHidden : r.afterHidden;
            t.hideElementId = a.isArray(t.hideElementId) ? t.hideElementId : r.hideElementId;
            if (t.position == "left" || t.position == "right") {
                t.align = o(t.align, r.alignVerticalValues) ? t.align : "middle"
            } else {
                t.align = o(t.align, r.alignHorizontalValues) ? t.align : "center"
            }
            for (var u in t.tail) {
                switch (u) {
                    case "align":
                        t.tail.align = typeof t.tail.align == "string" && o(t.tail.align.toLowerCase(), r.alignValues) ? t.tail.align.toLowerCase() : r.tail.align;
                        if (t.position == "left" || t.position == "right") {
                            t.tail.align = o(t.tail.align, r.alignVerticalValues) ? t.tail.align : "middle"
                        } else {
                            t.tail.align = o(t.tail.align, r.alignHorizontalValues) ? t.tail.align : "center"
                        }
                        break;
                    case "hidden":
                        t.tail.hidden = t.tail.hidden == true ? true : false;
                        break
                }
            }
            return t
        }

        function l(t) {
            if (t == 0) {
                return 0
            }
            if (t > 0) {
                return -(Math.abs(t))
            } else {
                return Math.abs(t)
            }
        }

        function o(v, w) {
            var t = false;
            for (var u in w) {
                if (w[u] == v) {
                    t = true;
                    break
                }
            }
            return t
        }

        function k(t) {
            if (document.createElement) {
                for (var v = t.length - 1; v >= 0; v--) {
                    var u = document.createElement("img");
                    u.src = t[v];
                    if (a.inArray(t[v], r.cache) > -1) {
                        r.cache.push(t[v])
                    }
                }
            }
        }

        function b(t) {
            if (t.hideElementId && t.hideElementId.length > 0) {
                for (var u = 0; u < t.hideElementId.length; u++) {
                    var v = (t.hideElementId[u].charAt(0) != "#" ? "#" + t.hideElementId[u] : t.hideElementId[u]);
                    a(v).css({
                        visibility:"hidden"
                    })
                }
            }
        }

        function s(u) {
            if (u.hideElementId && u.hideElementId.length > 0) {
                for (var v = 0; v < u.hideElementId.length; v++) {
                    var x = (u.hideElementId[v].charAt(0) != "#" ? "#" + u.hideElementId[v] : u.hideElementId[v]);
                    a(x).css({
                        visibility:"visible"
                    });
                    var w = a(x).length;
                    for (var t = 0; t < w.length; t++) {
                        a(w[t]).css({
                            visibility:"visible"
                        })
                    }
                }
            }
        }

        function m(u) {
            var w = u.themePath;
            var t = u.themeName;
            var v = (w.substring(w.length - 1) == "/" || w.substring(w.length - 1) == "\\") ? w.substring(0, w.length - 1) + "/" + t + "/" : w + "/" + t + "/";
            return v
        }

        function j(t) {
            var u = t.substring(0, 1) == "." ? t.substring(1, t.length) : t;
            return u
        }

        function q(u) {
            if (a("#" + u.privateVars.id).length > 0) {
                var t = "bottom-middle";
                switch (u.position) {
                    case "left":
                        t = "middle-right";
                        break;
                    case "top":
                        t = "bottom-middle";
                        break;
                    case "right":
                        t = "middle-left";
                        break;
                    case "bottom":
                        t = "top-middle";
                        break
                }
                if (o(u.tail.align, r.alignHorizontalValues)) {
                    a("#" + u.privateVars.id).find("td." + u.baseClass + "-" + t).css("text-align", u.tail.align)
                } else {
                    a("#" + u.privateVars.id).find("td." + u.baseClass + "-" + t).css("vertical-align", u.tail.align)
                }
            }
        }

        function p(v) {
            var H = r.model_markup;
            var F = m(v);
            var x = "";
            var G = "";
            var u = "";
            if (!v.tail.hidden) {
                switch (v.position) {
                    case "left":
                        G = "right";
                        u = "{MIDDLE-RIGHT}";
                        break;
                    case "top":
                        G = "bottom";
                        u = "{BOTTOM-MIDDLE}";
                        break;
                    case "right":
                        G = "left";
                        u = "{MIDDLE-LEFT}";
                        break;
                    case "bottom":
                        G = "top";
                        u = "{TOP-MIDDLE}";
                        break
                }
                x = '<img src="' + F + "tail-" + G + ".png" + '" alt="" class="' + v.baseClass + '-tail" />'
            }
            var t = r.model_tr;
            var z = r.model_td;
            var K, E, A, J;
            var B = "";
            var y = "";
            var D = new Array();
            for (E in t) {
                A = "";
                J = "";
                for (K in z) {
                    A = t[E] + "-" + z[K];
                    A = A.toUpperCase();
                    J = "{" + A + "_STYLE}";
                    A = "{" + A + "}";
                    if (A == u) {
                        H = H.replace(A, x);
                        B = ""
                    } else {
                        H = H.replace(A, "");
                        B = ""
                    }
                    if (t[E] + "-" + z[K] != "middle-middle") {
                        y = F + t[E] + "-" + z[K] + ".png";
                        D.push(y);
                        H = H.replace(J, ' style="' + B + "background-image:url(" + y + ');"')
                    }
                }
            }
            if (D.length > 0) {
                k(D)
            }
            var w = "";
            if (v.tableStyle != null && typeof v.tableStyle == "object" && !a.isArray(v.tableStyle) && !a.isEmptyObject(v.tableStyle)) {
                for (var C in v.tableStyle) {
                    w += C + ":" + v.tableStyle[C] + ";"
                }
            }
            w += (v.width != null || v.height != null) ? (v.width != null ? "width:" + v.width + "px;" : "") + (v.height != null ? "height:" + v.height + "px;" : "") : "";
            H = w.length > 0 ? H.replace("{TABLE_STYLE}", ' style="' + w + '"') : H.replace("{TABLE_STYLE}", "");
            var I = "";
            if (v.divStyle != null && typeof v.divStyle == "object" && !a.isArray(v.divStyle) && !a.isEmptyObject(v.divStyle)) {
                for (var C in v.divStyle) {
                    I += C + ":" + v.divStyle[C] + ";"
                }
            }
            H = I.length > 0 ? H.replace("{DIV_STYLE}", ' style="' + I + '"') : H.replace("{DIV_STYLE}", "");
            H = H.replace("{TEMPLATE_CLASS}", v.baseClass + "-" + v.themeName);
            H = v.privateVars.id != null ? H.replace("{DIV_ID}", v.privateVars.id) : H.replace("{DIV_ID}", "");
            while (H.indexOf("{BASE_CLASS}") > -1) {
                H = H.replace("{BASE_CLASS}", v.baseClass)
            }
            H = v.innerHtml != null ? H.replace("{INNERHTML}", v.innerHtml) : H.replace("{INNERHTML}", "");
            J = "";
            for (var C in v.innerHtmlStyle) {
                J += C + ":" + v.innerHtmlStyle[C] + ";"
            }
            H = J.length > 0 ? H.replace("{INNERHTML_STYLE}", ' style="' + J + '"') : H.replace("{INNERHTML_STYLE}", "");
            return H
        }

        function f() {
            return Math.round(new Date().getTime() / 1000)
        }

        function c(E, N, x) {
            var O = x.position;
            var K = x.align;
            var z = x.distance;
            var F = x.themeMargins;
            var I = new Array();
            var u = N.offset();
            var t = parseInt(u.top);
            var y = parseInt(u.left);
            var P = parseInt(N.outerWidth(false));
            var L = parseInt(N.outerHeight(false));
            var v = parseInt(E.outerWidth(false));
            var M = parseInt(E.outerHeight(false));
            F.difference = Math.abs(parseInt(F.difference));
            F.total = Math.abs(parseInt(F.total));
            var w = l(F.difference);
            var J = l(F.difference);
            var A = l(F.total);
            var H = m(x);
            switch (K) {
                case "left":
                    I.top = O == "top" ? t - M - z + l(w) : t + L + z + w;
                    I.left = y + A;
                    break;
                case "center":
                    var D = Math.abs(v - P) / 2;
                    I.top = O == "top" ? t - M - z + l(w) : t + L + z + w;
                    I.left = v >= P ? y - D : y + D;
                    break;
                case "right":
                    var D = Math.abs(v - P);
                    I.top = O == "top" ? t - M - z + l(w) : t + L + z + w;
                    I.left = v >= P ? y - D + l(A) : y + D + l(A);
                    break;
                case "top":
                    I.top = t + A;
                    I.left = O == "left" ? y - v - z + l(J) : y + P + z + J;
                    break;
                case "middle":
                    var D = Math.abs(M - L) / 2;
                    I.top = M >= L ? t - D : t + D;
                    I.left = O == "left" ? y - v - z + l(J) : y + P + z + J;
                    break;
                case "bottom":
                    var D = Math.abs(M - L);
                    I.top = M >= L ? t - D + l(A) : t + D + l(A);
                    I.left = O == "left" ? y - v - z + l(J) : y + P + z + J;
                    break
            }
            I.position = O;
            if (a("#" + x.privateVars.id).length > 0 && a("#" + x.privateVars.id).find("img." + x.baseClass + "-tail").length > 0) {
                a("#" + x.privateVars.id).find("img." + x.baseClass + "-tail").remove();
                var G = "bottom";
                var C = "bottom-middle";
                switch (O) {
                    case "left":
                        G = "right";
                        C = "middle-right";
                        break;
                    case "top":
                        G = "bottom";
                        C = "bottom-middle";
                        break;
                    case "right":
                        G = "left";
                        C = "middle-left";
                        break;
                    case "bottom":
                        G = "top";
                        C = "top-middle";
                        break
                }
                a("#" + x.privateVars.id).find("td." + x.baseClass + "-" + C).empty();
                a("#" + x.privateVars.id).find("td." + x.baseClass + "-" + C).html('<img src="' + H + "tail-" + G + ".png" + '" alt="" class="' + x.baseClass + '-tail" />');
                q(x)
            }
            if (x.alwaysVisible == true) {
                if (I.top < a(window).scrollTop() || I.top + M > a(window).scrollTop() + a(window).height()) {
                    if (a("#" + x.privateVars.id).length > 0 && a("#" + x.privateVars.id).find("img." + x.baseClass + "-tail").length > 0) {
                        a("#" + x.privateVars.id).find("img." + x.baseClass + "-tail").remove()
                    }
                    var B = "";
                    if (I.top < a(window).scrollTop()) {
                        I.position = "bottom";
                        I.top = t + L + z + w;
                        if (a("#" + x.privateVars.id).length > 0 && !x.tail.hidden) {
                            a("#" + x.privateVars.id).find("td." + x.baseClass + "-top-middle").empty();
                            a("#" + x.privateVars.id).find("td." + x.baseClass + "-top-middle").html('<img src="' + H + "tail-top.png" + '" alt="" class="' + x.baseClass + '-tail" />');
                            B = "top-middle"
                        }
                    } else {
                        if (I.top + M > a(window).scrollTop() + a(window).height()) {
                            I.position = "top";
                            I.top = t - M - z + l(w);
                            if (a("#" + x.privateVars.id).length > 0 && !x.tail.hidden) {
                                a("#" + x.privateVars.id).find("td." + x.baseClass + "-bottom-middle").empty();
                                a("#" + x.privateVars.id).find("td." + x.baseClass + "-bottom-middle").html('<img src="' + H + "tail-bottom.png" + '" alt="" class="' + x.baseClass + '-tail" />');
                                B = "bottom-middle"
                            }
                        }
                    }
                    if (I.left < 0) {
                        I.left = 0;
                        if (B.length > 0) {
                            a("#" + x.privateVars.id).find("td." + x.baseClass + "-" + B).css("text-align", "center")
                        }
                    } else {
                        if (I.left + v > a(window).width()) {
                            I.left = a(window).width() - v;
                            if (B.length > 0) {
                                a("#" + x.privateVars.id).find("td." + x.baseClass + "-" + B).css("text-align", "center")
                            }
                        }
                    }
                } else {
                    if (I.left < 0 || I.left + v > a(window).width()) {
                        if (a("#" + x.privateVars.id).length > 0 && a("#" + x.privateVars.id).find("img." + x.baseClass + "-tail").length > 0) {
                            a("#" + x.privateVars.id).find("img." + x.baseClass + "-tail").remove()
                        }
                        var B = "";
                        if (I.left < 0) {
                            I.position = "right";
                            I.left = y + P + z + J;
                            if (a("#" + x.privateVars.id).length > 0 && !x.tail.hidden) {
                                a("#" + x.privateVars.id).find("td." + x.baseClass + "-middle-left").empty();
                                a("#" + x.privateVars.id).find("td." + x.baseClass + "-middle-left").html('<img src="' + H + "tail-left.png" + '" alt="" class="' + x.baseClass + '-tail" />');
                                B = "middle-left"
                            }
                        } else {
                            if (I.left + v > a(window).width()) {
                                I.position = "left";
                                I.left = y - v - z + l(J);
                                if (a("#" + x.privateVars.id).length > 0 && !x.tail.hidden) {
                                    a("#" + x.privateVars.id).find("td." + x.baseClass + "-middle-right").empty();
                                    a("#" + x.privateVars.id).find("td." + x.baseClass + "-middle-right").html('<img src="' + H + "tail-right.png" + '" alt="" class="' + x.baseClass + '-tail" />');
                                    B = "middle-right"
                                }
                            }
                        }
                        if (I.top < a(window).scrollTop()) {
                            I.top = a(window).scrollTop();
                            if (B.length > 0) {
                                a("#" + x.privateVars.id).find("td." + x.baseClass + "-" + B).css("vertical-align", "middle")
                            }
                        } else {
                            if (I.top + M > a(window).scrollTop() + a(window).height()) {
                                I.top = (a(window).scrollTop() + a(window).height()) - M;
                                if (B.length > 0) {
                                    a("#" + x.privateVars.id).find("td." + x.baseClass + "-" + B).css("vertical-align", "middle")
                                }
                            }
                        }
                    }
                }
            }
            return I
        }

        function d(u, t) {
            a(u).data(r.options_key, t)
        }

        function n(t) {
            return a(t).data(r.options_key)
        }

        function i(t) {
            var u = t != null && typeof t == "object" && !a.isArray(t) && !a.isEmptyObject(t) ? true : false;
            return u
        }

        function h(t) {
            /*      Kuali customization below - we handle these ourselves:

             a(window).resize(function () {
             a(r.me).each(function (u, v) {
             a(v).trigger("positionpopover")
             })
             });
             a(document).mousemove(function (u) {
             a(r.me).each(function (v, w) {
             a(w).trigger("managepopover", [u.pageX, u.pageY])
             })
             });*/
            a(r.me).each(function (v, w) {
                var u = g(t);
                u.privateVars.creation_datetime = f();
                u.privateVars.id = u.baseClass + "-" + u.privateVars.creation_datetime + "-" + v;
                d(w, u);
                a(w).bind("managepopover", function (y, C, B) {
                    var N = n(this);
                    if (i(N) && i(N.privateVars) && typeof C != "undefined" && typeof B != "undefined") {
                        if (N.manageMouseEvents) {
                            var E = a(this);
                            var z = E.offset();
                            var L = parseInt(z.top);
                            var H = parseInt(z.left);
                            var F = parseInt(E.outerWidth(false));
                            var K = parseInt(E.outerHeight(false));
                            var J = false;
                            if (H <= C && C <= F + H && L <= B && B <= K + L) {
                                J = true
                            } else {
                                J = false
                            }
                            if (J && !N.privateVars.is_mouse_over) {
                                N.privateVars.is_mouse_over = true;
                                d(this, N);
                                if (N.mouseMove == "show") {
                                    a(this).trigger("showpopover")
                                } else {
                                    if (N.selectable && a("#" + N.privateVars.id).length > 0) {
                                        var x = a("#" + N.privateVars.id);
                                        var A = x.offset();
                                        var D = parseInt(A.top);
                                        var I = parseInt(A.left);
                                        var G = parseInt(x.outerWidth(false));
                                        var M = parseInt(x.outerHeight(false));
                                        if (I <= C && C <= G + I && D <= B && B <= M + D) {
                                        } else {
                                            a(this).trigger("hidepopover")
                                        }
                                    } else {
                                        a(this).trigger("hidepopover")
                                    }
                                }
                            } else {
                                if (!J && N.privateVars.is_mouse_over) {
                                    N.privateVars.is_mouse_over = false;
                                    d(this, N);
                                    if (N.mouseOut == "show") {
                                        a(this).trigger("showpopover")
                                    } else {
                                        if (N.selectable && a("#" + N.privateVars.id).length > 0) {
                                            var x = a("#" + N.privateVars.id);
                                            var A = x.offset();
                                            var D = parseInt(A.top);
                                            var I = parseInt(A.left);
                                            var G = parseInt(x.outerWidth(false));
                                            var M = parseInt(x.outerHeight(false));
                                            if (I <= C && C <= G + I && D <= B && B <= M + D) {
                                            } else {
                                                a(this).trigger("hidepopover")
                                            }
                                        } else {
                                            a(this).trigger("hidepopover")
                                        }
                                    }
                                } else {
                                    if (!J && !N.privateVars.is_mouse_over) {
                                        if (N.selectable && a("#" + N.privateVars.id).length > 0 && !N.privateVars.is_animating) {
                                            var x = a("#" + N.privateVars.id);
                                            var A = x.offset();
                                            var D = parseInt(A.top);
                                            var I = parseInt(A.left);
                                            var G = parseInt(x.outerWidth(false));
                                            var M = parseInt(x.outerHeight(false));
                                            if (I <= C && C <= G + I && D <= B && B <= M + D) {
                                            } else {
                                                a(this).trigger("hidepopover")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
                a(w).bind("setpopoverinnerhtml", function (A, x, z) {
                    var y = n(this);
                    if (i(y) && i(y.privateVars) && typeof x != "undefined") {
                        y.privateVars.last_modified_datetime = f();
                        if (typeof z == "boolean" && z == true) {
                            y.innerHtml = x
                        }
                        d(this, y);
                        if (a("#" + y.privateVars.id).length > 0) {
                            a("#" + y.privateVars.id).find("td." + y.baseClass + "-innerHtml").html(x);
                            if (y.privateVars.is_animation_complete) {
                                a(this).trigger("positionpopover", [false])
                            } else {
                                a(this).trigger("positionpopover", [true])
                            }
                        }
                    }
                });
                a(w).bind("setpopoveroptions", function (A, z) {
                    var x = n(this);
                    if (i(x) && i(x.privateVars)) {
                        var y = x;
                        x = g(z);
                        x.privateVars.id = y.privateVars.id;
                        x.privateVars.creation_datetime = y.privateVars.creation_datetime;
                        x.privateVars.last_modified_datetime = f();
                        x.privateVars.last_display_datetime = y.privateVars.last_display_datetime;
                        x.privateVars.is_open = y.privateVars.is_open;
                        x.privateVars.is_freezed = y.privateVars.is_freezed;
                        x.privateVars.last_options = {};
                        d(this, x)
                    }
                });
                a(w).bind("positionpopover", function (A, y) {
                    var z = n(this);
                    if (i(z) && i(z.privateVars) && a("#" + z.privateVars.id).length > 0 && z.privateVars.is_open == true) {
                        var x = a("#" + z.privateVars.id);
                        var C = c(x, a(this), z);
                        var B = 0; // 2; // Setting to "2" makes the popover shift when inner html is set after opening.
                        if (typeof y == "boolean" && y == true) {
                            x.css({
                                top:C.top,
                                left:C.left
                            })
                        } else {
                            switch (z.position) {
                                case "left":
                                    x.css({
                                        top:C.top,
                                        left:(C.position != z.position ? C.left - (Math.abs(z.themeMargins.difference) * B) : C.left + (Math.abs(z.themeMargins.difference) * B))
                                    });
                                    break;
                                case "top":
                                    x.css({
                                        top:(C.position != z.position ? C.top - (Math.abs(z.themeMargins.difference) * B) : C.top + (Math.abs(z.themeMargins.difference) * B)),
                                        left:C.left
                                    });
                                    break;
                                case "right":
                                    x.css({
                                        top:C.top,
                                        left:(C.position != z.position ? C.left + (Math.abs(z.themeMargins.difference) * B) : C.left - (Math.abs(z.themeMargins.difference) * B))
                                    });
                                    break;
                                case "bottom":
                                    x.css({
                                        top:(C.position != z.position ? C.top + (Math.abs(z.themeMargins.difference) * B) : C.top - (Math.abs(z.themeMargins.difference) * B)),
                                        left:C.left
                                    });
                                    break
                            }
                        }
                    }
                });
                a(w).bind("freezepopover", function () {
                    var x = n(this);
                    if (i(x) && i(x.privateVars)) {
                        x.privateVars.is_freezed = true;
                        d(this, x)
                    }
                });
                a(w).bind("unfreezepopover", function () {
                    var x = n(this);
                    if (i(x) && i(x.privateVars)) {
                        x.privateVars.is_freezed = false;
                        d(this, x)
                    }
                });
                a(w).bind("showpopover", function (x, A, D, G) {
                    var H = n(this);
                    if ((typeof G == "boolean" && G == true && (i(H) && i(H.privateVars))) || (typeof G == "undefined" && (i(H) && i(H.privateVars) && !H.privateVars.is_freezed && !H.privateVars.is_open))) {
                        if (typeof G == "boolean" && G == true) {
                            a(this).trigger("unfreezepopover")
                        }
                        H.privateVars.is_open = true;
                        H.privateVars.is_freezed = false;
                        H.privateVars.is_animating = false;
                        H.privateVars.is_animation_complete = false;
                        if (i(H.privateVars.last_options)) {
                            H = H.privateVars.last_options
                        } else {
                            H.privateVars.last_options = {}
                        }
                        if (i(A)) {
                            var C = H;
                            var F = f();
                            H = g(A);
                            H.privateVars.id = C.privateVars.id;
                            H.privateVars.creation_datetime = C.privateVars.creation_datetime;
                            H.privateVars.last_modified_datetime = F;
                            H.privateVars.last_display_datetime = F;
                            H.privateVars.is_open = true;
                            H.privateVars.is_freezed = false;
                            H.privateVars.is_animating = false;
                            H.privateVars.is_animation_complete = false;
                            H.privateVars.is_mouse_over = C.privateVars.is_mouse_over;
                            H.privateVars.is_position_changed = C.privateVars.is_position_changed;
                            H.privateVars.last_options = {};
                            if (typeof D == "boolean" && D == false) {
                                C.privateVars.last_modified_datetime = F;
                                C.privateVars.last_display_datetime = F;
                                H.privateVars.last_options = C
                            }
                        }
                        d(this, H);
                        b(H);
                        if (a("#" + H.privateVars.id).length > 0) {
                            a("#" + H.privateVars.id).remove()
                        }
                        var y = {};
                        var B = p(H);
                        y = a(B);
                        y.appendTo("body");
                        y = a("#" + H.privateVars.id);
                        y.css({
                            opacity:0,
                            top:"0px",
                            left:"0px",
                            position:"absolute",
                            display:"block"
                        });
                        q(H);
                        var E = c(y, a(this), H);
                        y.css({
                            top:E.top,
                            left:E.left
                        });
                        if (E.position == H.position) {
                            H.privateVars.is_position_changed = false
                        } else {
                            H.privateVars.is_position_changed = true
                        }
                        d(this, H);
                        var z = setTimeout(function () {
                            H.privateVars.is_animating = true;
                            d(w, H);
                            y.stop();
                            switch (H.position) {
                                case "left":
                                    y.animate({
                                        left:(H.privateVars.is_position_changed ? "-=" : "+=") + H.distance + "px",
                                        opacity:1
                                    }, H.openingSpeed, "swing", function () {
                                        H.privateVars.is_animating = false;
                                        H.privateVars.is_animation_complete = true;
                                        d(w, H);
                                        H.afterShown()
                                    });
                                    break;
                                case "top":
                                    y.animate({
                                        top:(H.privateVars.is_position_changed ? "-=" : "+=") + H.distance + "px",
                                        opacity:1
                                    }, H.openingSpeed, "swing", function () {
                                        H.privateVars.is_animating = false;
                                        H.privateVars.is_animation_complete = true;
                                        d(w, H);
                                        H.afterShown()
                                    });
                                    break;
                                case "right":
                                    y.animate({
                                        left:(H.privateVars.is_position_changed ? "+=" : "-=") + H.distance + "px",
                                        opacity:1
                                    }, H.openingSpeed, "swing", function () {
                                        H.privateVars.is_animating = false;
                                        H.privateVars.is_animation_complete = true;
                                        d(w, H);
                                        H.afterShown()
                                    });
                                    break;
                                case "bottom":
                                    y.animate({
                                        top:(H.privateVars.is_position_changed ? "+=" : "-=") + H.distance + "px",
                                        opacity:1
                                    }, H.openingSpeed, "swing", function () {
                                        H.privateVars.is_animating = false;
                                        H.privateVars.is_animation_complete = true;
                                        d(w, H);
                                        H.afterShown()
                                    });
                                    break
                            }
                        }, H.openingDelay)
                    }
                });
                a(w).bind("hidepopover", function (B, x) {
                    var A = n(this);
                    if ((typeof x == "boolean" && x == true && (i(A) && i(A.privateVars) && a("#" + A.privateVars.id).length > 0)) || (typeof x == "undefined" && (i(A) && i(A.privateVars) && a("#" + A.privateVars.id).length > 0 && !A.privateVars.is_freezed && A.privateVars.is_open))) {
                        if (typeof x == "boolean" && x == true) {
                            a(this).trigger("unfreezepopover")
                        }
                        A.privateVars.is_animating = false;
                        A.privateVars.is_animation_complete = false;
                        d(this, A);
                        var y = a("#" + A.privateVars.id);
                        var z = typeof x == "undefined" ? A.closingDelay : 0;
                        var C = setTimeout(function () {
                            A.privateVars.is_animating = true;
                            d(w, A);
                            y.stop();
                            switch (A.position) {
                                case "left":
                                    y.animate({
                                        opacity:0,
                                        left:(A.privateVars.is_position_changed ? "+=" : "-=") + A.distance + "px"
                                    }, A.closingSpeed, "swing", function () {
                                        A.privateVars.is_open = false;
                                        A.privateVars.is_animating = false;
                                        A.privateVars.is_animation_complete = true;
                                        d(w, A);
                                        y.css("display", "none");
                                        A.afterHidden()
                                    });
                                    break;
                                case "top":
                                    y.animate({
                                        opacity:0,
                                        top:(A.privateVars.is_position_changed ? "+=" : "-=") + A.distance + "px"
                                    }, A.closingSpeed, "swing", function () {
                                        A.privateVars.is_open = false;
                                        A.privateVars.is_animating = false;
                                        A.privateVars.is_animation_complete = true;
                                        d(w, A);
                                        y.css("display", "none");
                                        A.afterHidden()
                                    });
                                    break;
                                case "right":
                                    y.animate({
                                        opacity:0,
                                        left:(A.privateVars.is_position_changed ? "-=" : "+=") + A.distance + "px"
                                    }, A.closingSpeed, "swing", function () {
                                        A.privateVars.is_open = false;
                                        A.privateVars.is_animating = false;
                                        A.privateVars.is_animation_complete = true;
                                        d(w, A);
                                        y.css("display", "none");
                                        A.afterHidden()
                                    });
                                    break;
                                case "bottom":
                                    y.animate({
                                        opacity:0,
                                        top:(A.privateVars.is_position_changed ? "-=" : "+=") + A.distance + "px"
                                    }, A.closingSpeed, "swing", function () {
                                        A.privateVars.is_open = false;
                                        A.privateVars.is_animating = false;
                                        A.privateVars.is_animation_complete = true;
                                        d(w, A);
                                        y.css("display", "none");
                                        A.afterHidden()
                                    });
                                    break
                            }
                        }, z);
                        A.privateVars.last_display_datetime = f();
                        A.privateVars.is_freezed = false;
                        d(this, A);
                        s(A)
                    }
                })
            })
        }

        return this
    }
})(jQuery);