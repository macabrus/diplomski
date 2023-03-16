import { Accessor, createRenderEffect, Setter, Signal } from "solid-js";
import { DOMElement } from "solid-js/jsx-runtime";

declare module "solid-js" {
  namespace JSX {
    interface Directives {
      model: Signal<string>;
      fileModel: Signal<UploadDescriptor[]>;
      boolModel: Signal<boolean>;
      numModel: Signal<number>;
    }
  }
}

function fromEvent(setter: (val: any) => void) {
  return function (e: any) {
    setter(e.currentTarget.value);
  };
}

const toBase64 = async (file: any) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsArrayBuffer(file);
    reader.onload = () => {
      let binary = "";
      let bytes = new Uint8Array(reader.result as ArrayBuffer);
      for (let i = 0; i < bytes.length; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      resolve(btoa(binary));
    };
    reader.onerror = (error) => reject(error);
  });

function fileFromEvent(setter: (val: any) => void) {
  return async function (e: any) {
    setter(e.currentTarget.files);
  };
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

function intFromEvent(setter: Setter<number>) {
  return function (e: any) {
    const el = e.currentTarget;
    if (el.type && el.type === "checkbox") {
      return setter(el.checked);
    }
    return setter(Number.parseInt(e.currentTarget.value));
  };
}
type Model<T> = () => [() => T, (val: T) => void];
type UploadDescriptor = {name: string, type: string, size: number, content: string};
type FileModel = Model<UploadDescriptor[]>;

export function numModel(el: any, value: Model<number>) {
  const [getter, setter] = value();
  return model(el, () => [getter, (val) => setter(Number.parseInt(val as any))]);
}

export function boolModel(el: any, value: Model<boolean>) {
  const [getter, setter] = value();
  return model(el, () => [getter, (val) => setter(!!val)]);
}

export function fileModel(el: any, value: FileModel) {
  const [getter, setter] = value();
  /* Extracts files from file input */
  async function fileParser(files: any) {
    const descriptors: any[] = [];
    console.log(files);
    for (let file of files) {
      descriptors.push({
        name: file.name,
        type: file.type,
        size: file.size,
        content: await toBase64(file),
      });
    }
    return descriptors;
  }
  return model(el, () => [getter, async (val) => setter(await fileParser(val))])
}

export function model<T>(el: any, value: Model<T>) {
  const [getter, setter] = value();
  const tag = (el.tagName || "").toLowerCase();
  if (tag === "input") {
    const type = (el.getAttribute("type") || "text").toLocaleLowerCase();
    switch (type) {
      case "password":
      case "color":
      case "colour":
      case "text": {
        console.log("registering");
        createRenderEffect(() => (el.value = getter()));
        el.addEventListener("input", fromEvent(setter));
        break;
      }
      case "range": {
        console.log("registering range!");
        createRenderEffect(() => (el.value = getter()));
        el.addEventListener("input", fromEvent(setter));
      }
      case "radio": {
        const radioGroup = el.getAttribute("name");
        const parentForm = el.closest("form") || document;
        const radios = [
          ...parentForm.querySelectorAll(`input[name="${radioGroup}"]`),
        ];
        for (let radio of radios) {
          radio.addEventListener("input", fromEvent(setter));
          if (radio.value === getter()) {
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
        el.checked = getter();
        el.addEventListener("input", (e) => setter(e.currentTarget.checked));
        break;
      }
      case "file": {
        el.addEventListener("input", fileFromEvent(setter));
        break;
      }
    }
  } else if (tag === 'select') {
    console.log('should attach');
    el.addEventListener("input", fromEvent(setter));
  }
}
