export function getNumber(name, getValue, digits, suffix) {
	return { name, align: "r", getValue, render: r => r.value != null ? r.value.toFixed(digits) + suffix : "" };
}

export function getAmount(name, getValue) {
	return getNumber(name, getValue, 2, "");
}

export function getPct(name, getValue, digits = 2) {
	return getNumber(name, getValue, digits, "%");
}

export function getPctChange(name, getValue) {
	return {
		name,
		align: "r",
		getValue,
		render: r => (
			<td key={r.key} className={r.column.align} style={{ color: r.value >= 0 ? "green" : "#C00" }}>
				{Math.abs(r.value).toFixed(2)}%
			</td>
		)
	};
}
