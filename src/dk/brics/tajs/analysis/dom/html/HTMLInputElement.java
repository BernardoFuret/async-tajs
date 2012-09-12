/*
 * Copyright 2012 Aarhus University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.brics.tajs.analysis.dom.html;

import dk.brics.tajs.analysis.*;
import dk.brics.tajs.analysis.dom.DOMObjects;
import dk.brics.tajs.analysis.dom.DOMWindow;
import dk.brics.tajs.lattice.ObjectLabel;
import dk.brics.tajs.lattice.Value;
import dk.brics.tajs.util.AnalysisException;

import static dk.brics.tajs.analysis.dom.DOMFunctions.createDOMFunction;
import static dk.brics.tajs.analysis.dom.DOMFunctions.createDOMProperty;

public class HTMLInputElement {

    public static ObjectLabel CONSTRUCTOR;
    public static ObjectLabel PROTOTYPE;
    public static ObjectLabel INSTANCES;

    public static void build(State s) {
        CONSTRUCTOR = new ObjectLabel(DOMObjects.HTMLINPUTELEMENT_CONSTRUCTOR, ObjectLabel.Kind.FUNCTION);
        PROTOTYPE = new ObjectLabel(DOMObjects.HTMLINPUTELEMENT_PROTOTYPE, ObjectLabel.Kind.OBJECT);
        INSTANCES = new ObjectLabel(DOMObjects.HTMLINPUTELEMENT_INSTANCES, ObjectLabel.Kind.OBJECT);

        // Constructor Object
        s.newObject(CONSTRUCTOR);
        s.writePropertyWithAttributes(CONSTRUCTOR, "length", Value.makeNum(0).setAttributes(true, true, true));
        s.writePropertyWithAttributes(CONSTRUCTOR, "prototype", Value.makeObject(PROTOTYPE).setAttributes(true, true, true));
        s.writeInternalPrototype(CONSTRUCTOR, Value.makeObject(InitialStateBuilder.FUNCTION_PROTOTYPE));
        s.writeProperty(DOMWindow.WINDOW, "HTMLInputElement", Value.makeObject(CONSTRUCTOR));

        // Prototype Object
        s.newObject(PROTOTYPE);
        s.writeInternalPrototype(PROTOTYPE, Value.makeObject(HTMLElement.ELEMENT_PROTOTYPE));

        // Multiplied Object
        s.newObject(INSTANCES);
        s.writeInternalPrototype(INSTANCES, Value.makeObject(PROTOTYPE));

        /*
         * Properties.
         */
        // DOM Level 1
        createDOMProperty(s, INSTANCES, "defaultValue", Value.makeAnyStr());
        createDOMProperty(s, INSTANCES, "defaultChecked", Value.makeAnyBool());
        createDOMProperty(s, INSTANCES, "form", Value.makeObject(HTMLFormElement.INSTANCES).setReadOnly());
        createDOMProperty(s, INSTANCES, "accept", Value.makeAnyStr());
        createDOMProperty(s, INSTANCES, "accessKey", Value.makeAnyStr());
        createDOMProperty(s, INSTANCES, "align", Value.makeAnyStr());
        createDOMProperty(s, INSTANCES, "alt", Value.makeAnyStr());
        createDOMProperty(s, INSTANCES, "checked", Value.makeAnyBool());
        createDOMProperty(s, INSTANCES, "disabled", Value.makeAnyBool());
        createDOMProperty(s, INSTANCES, "maxLength", Value.makeAnyNum());
        createDOMProperty(s, INSTANCES, "name", Value.makeAnyStr());
        createDOMProperty(s, INSTANCES, "readOnly", Value.makeAnyBool());
        createDOMProperty(s, INSTANCES, "src", Value.makeAnyStr());
        createDOMProperty(s, INSTANCES, "tabIndex", Value.makeAnyNum());
        createDOMProperty(s, INSTANCES, "useMap", Value.makeAnyStr());
        createDOMProperty(s, INSTANCES, "value", Value.makeAnyStr());

        // DOM Level 2
        createDOMProperty(s, INSTANCES, "size", Value.makeAnyNum());
        createDOMProperty(s, INSTANCES, "type", Value.makeAnyStr());

        s.multiplyObject(INSTANCES);
        INSTANCES = INSTANCES.makeSingleton().makeSummary();

        /*
         * Functions.
         */
        // DOM Level 1
        createDOMFunction(s, PROTOTYPE, DOMObjects.HTMLINPUTELEMENT_BLUR, "blur", 0);
        createDOMFunction(s, PROTOTYPE, DOMObjects.HTMLINPUTELEMENT_FOCUS, "focus", 0);
        createDOMFunction(s, PROTOTYPE, DOMObjects.HTMLINPUTELEMENT_SELECT, "select", 0);
        createDOMFunction(s, PROTOTYPE, DOMObjects.HTMLINPUTELEMENT_CLICK, "click", 0);
    }

    /**
     * Transfer Functions.
     */
    public static Value evaluate(DOMObjects nativeObject, FunctionCalls.CallInfo call, State s, Solver.SolverInterface c) {
        switch (nativeObject) {
            case HTMLINPUTELEMENT_BLUR: {
                NativeFunctions.expectParameters(nativeObject, call, c, 0, 0);
                return Value.makeUndef();
            }
            case HTMLINPUTELEMENT_CLICK: {
                NativeFunctions.expectParameters(nativeObject, call, c, 0, 0);
                return Value.makeUndef();
            }
            case HTMLINPUTELEMENT_FOCUS: {
                NativeFunctions.expectParameters(nativeObject, call, c, 0, 0);
                return Value.makeUndef();
            }
            case HTMLINPUTELEMENT_SELECT: {
                NativeFunctions.expectParameters(nativeObject, call, c, 0, 0);
                return Value.makeUndef();
            }
            default: {
                throw new AnalysisException("Unsupported Native Object: " + nativeObject);
            }
        }
    }

}
