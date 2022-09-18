import DataTable from './DataTable';
import { getAmount, getLink } from "./tableUtils";

const columns = [
	getLink("Ticker", p => p.symbol.split(" ")[0], r => `https://finance.yahoo.com/quote/${r.value}`),
	{ name: "#", align: "r", getValue: p => Math.abs(p.quantity), sortDirection: -1 },
	{ name: "Expiry", align: "c", getValue: p => `${p.symbol.split(" ")[1]} (${p.dte})`, sortDirection: 1 },
	getAmount("Strike", p => Number(p.symbol.split(" ")[2])),
	getAmount("Price", p => p.underlyingPrice ? p.underlyingPrice : 0),
	{
		name: "ITM",
		align: "c",
		getValue: p => p.inTheMoney,
		render: r => r.value ? (<td key={r.key} className={r.column.align}><i className="bi bi-check" style={{ marginLeft: 6 }}></i></td>) : null
	},
	getAmount("Prem.", p => Math.abs(p.costBasis))
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
