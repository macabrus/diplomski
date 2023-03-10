import { Accessor, createRenderEffect, Setter, Signal } from "solid-js";
import { DOMElement } from "solid-js/jsx-runtime";

declare module "solid-js" {
  namespace JSX {
    interface Directives {
      model: Signal<string>;
      boolModel: Signal<boolean>;
    }
  }
}

function fromEvent(setter: Setter<String>) {
  return function (e: any) {
    setter(e.currentTarget.value);
  };
}

const toBase64 = async (file: any) => new Promise((resolve, reject) => {
  const reader = new FileReader();
  reader.readAsDataURL(file);
  reader.onload = () => resolve(reader.result);
  reader.onerror = error => reject(error);
});

function fileFromEvent(setter: Setter<object>) {
  return async function(e: any) {
    const files = [];
    console.log(e.currentTarget.files);
    for (let file of e.currentTarget.files) {
      files.push({
        name: file.name,
        type: file.type,
        size: file.size,
        content: await toBase64(file)
      });
    }
    setter(files);
  }
}

// function blobModel(el: any, value: () => [Accessor<>])

function boolFromEvent(setter: Setter<boolean>) {
  return function (e: any) {
    const el = e.currentTarget;
    if (el.type && el.type === "checkbox") {
      return setter(el.checked);
    }
    return setter(!!e.currentTarget.value);
  };
}

export function boolModel(
  el: any,
  value: () => [Accessor<boolean>, Setter<boolean>]
) {}

export function model<T>(el: any, value: () => [Accessor<T>, Setter<T>]) {
  const [field, setField] = value();
  const tag = (el.tagName || '').toLowerCase()
  if (tag === "input") {
    const type = (el.getAttribute("type") || 'text').toLocaleLowerCase();
    switch (type) {
      case "password":
      case "color":
      case "colour":
      case "text": {
        console.log('registering');
        createRenderEffect(() => (el.value = field()));
        el.addEventListener("input", fromEvent(setField));
        break;
      }
      case "radio": {
        const radioGroup = el.getAttribute("name");
        const parentForm = el.closest("form") || document;
        const radios = [
          ...parentForm.querySelectorAll(`input[name="${radioGroup}"]`),
        ];
        for (let radio of radios) {
          radio.addEventListener("input", fromEvent(setField));
          if (radio.value === field()) {
            // if field pre-set field value is
            // same as radio value, select it
            radio.checked = true;
          }
        }
        // radio buttons are grouped by 'name' attribute
        // and are scoped to form they are in
        break;
      }
      case "checkbox": {
        if (field()) {
        }
      }
      case "file": {
        el.addEventListener("input", fileFromEvent(setField));
      }
    }
  }
}
