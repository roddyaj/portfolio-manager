/**
 * @param {string} s 
 */
export function enumToCamelCase(s) {
	return s.split("_").map(t => t.charAt(0).toUpperCase() + t.substring(1).toLowerCase()).join(" ");
}
