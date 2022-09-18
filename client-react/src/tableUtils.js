export function getNumber(name, getValue, digits, suffix) {
	return { name, align: "r", getValue, render: r => r.value != null ? r.value.toFixed(digits) + suffix : "", sortDirection: -1 };
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
		render: r => r.value != null ? (
			<td key={r.key} style={{ color: r.value >= 0 ? "green" : "#C00" }}>
				{Math.abs(r.value).toFixed(2)}%
			</td>
		) : null,
		sortDirection: -1
	};
}

export function getLink(name, getValue, getUrl) {
	return {
		name,
		align: "l",
		getValue,
		render: r => r.value != null ? (
			<td key={r.key} className={r.column.align}>
				<a href={getUrl(r)}>{r.value}</a>
			</td>
		) : null,
		sortDirection: 1
	};
}
