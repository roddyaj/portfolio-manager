import DataTable from './DataTable'

const columns = [
	{
		name: "Ticker",
		align: "l",
		getValue: p => p.symbol.split(" ")[0],
		render: r => (<td key={r.key} className={r.column.align}><a href={`https://finance.yahoo.com/quote/${r.value}`}>{r.value}</a></td>)
	},
	{ name: "#", align: "r", getValue: p => Math.abs(p.quantity) },
	{ name: "Expiry", align: "c", getValue: p => `${p.symbol.split(" ")[1]} (${p.dte})` },
	{ name: "Strike", align: "r", getValue: p => Number(p.symbol.split(" ")[2]), render: r => r.value.toFixed(2) },
	{ name: "Price", align: "r", getValue: p => p.underlyingPrice ? p.underlyingPrice : 0, render: r => r.value.toFixed(2) },
	{
		name: "ITM",
		align: "c",
		getValue: p => p.inTheMoney,
		render: r => r.value ? (<td key={r.key} className={r.column.align}><i className="bi bi-check" style={{ marginLeft: 6 }}></i></td>) : null
	},
	{ name: "Prem.", align: "r", getValue: p => Math.abs(p.costBasis) }
];

function Options(props) {
	const { positions, isLong, type } = props;

	const optionPositions = positions.filter(p => p.symbol.includes(" ") && p.symbol.endsWith(type.charAt(0)) && ((!isLong && p.quantity < 0) || (isLong && p.quantity > 0)));

	if (optionPositions.length === 0) {
		return null;
	}

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">{`${isLong ? "Long" : "Short"} ${type}`}</div>
				<span style={{ marginLeft: 10 }}>({optionPositions.length})</span>
			</div>
			<DataTable columns={columns} records={optionPositions} />
		</div>
	);
}

export default Options;
