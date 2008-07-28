/**
 * Tag library for generating YUI related HTMLs.
 *
 * @author Kohsuke Kawaguchi
 */
package org.kohsuke.scotland.yui;

def submitButton(String value) {
    INPUT(TYPE:"submit",NAME:"Submit",VALUE:value,CLASS:"submit-button")
}