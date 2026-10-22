(function () {
  function reindex(container) {
    container.querySelectorAll("[data-line-row]").forEach(function (row, index) {
      row.querySelectorAll("input, select, textarea, label").forEach(function (el) {
        ["name", "id", "for"].forEach(function (attr) {
          var value = el.getAttribute(attr);
          if (!value) {
            return;
          }
          el.setAttribute(attr, value.replace(/lines\[\d+]/g, "lines[" + index + "]"));
        });
      });
    });
  }

  document.querySelectorAll("[data-line-editor]").forEach(function (editor) {
    editor.addEventListener("click", function (event) {
      var add = event.target.closest("[data-add-line]");
      var remove = event.target.closest("[data-remove-line]");
      if (add) {
        var template = editor.querySelector("[data-line-row]");
        var clone = template.cloneNode(true);
        clone.querySelectorAll("input, select").forEach(function (field) {
          if (field.tagName === "SELECT") {
            field.selectedIndex = 0;
          } else if (field.type !== "hidden") {
            field.value = "";
          }
        });
        editor.querySelector("[data-line-rows]").appendChild(clone);
        reindex(editor);
      }
      if (remove) {
        var rows = editor.querySelectorAll("[data-line-row]");
        if (rows.length === 1) {
          return;
        }
        remove.closest("[data-line-row]").remove();
        reindex(editor);
      }
    });
  });

  document.querySelectorAll("[data-location-form]").forEach(function (form) {
    var type = form.querySelector("[name='type']");
    var parent = form.querySelector("[name='parentLocationId']");
    var warehouse = form.querySelector("[name='warehouseId']");
    function sync() {
      var warehouseId = warehouse ? warehouse.value : "";
      var isBin = type && type.value === "BIN";
      if (parent) {
        parent.disabled = !isBin;
        parent.required = isBin;
        Array.prototype.forEach.call(parent.options, function (option) {
          if (!option.value) {
            option.hidden = false;
            return;
          }
          var sameWarehouse = !option.dataset.warehouse || option.dataset.warehouse === warehouseId;
          var isZone = option.dataset.type === "ZONE";
          option.hidden = !(sameWarehouse && isZone);
        });
        if (parent.selectedOptions[0] && parent.selectedOptions[0].hidden) {
          parent.value = "";
        }
      }
    }
    form.addEventListener("change", sync);
    sync();
  });

  document.querySelectorAll("form[data-confirm]").forEach(function (form) {
    form.addEventListener("submit", function (event) {
      if (!window.confirm(form.getAttribute("data-confirm"))) {
        event.preventDefault();
      }
    });
  });
})();
