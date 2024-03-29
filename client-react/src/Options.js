import DataTable from './DataTable';
import { getAmount, getLink } from "./tableUtils";

const columns = [
	getLink("Ticker", p => p.symbol, r => `https://finance.yahoo.com/quote/${r.value}`),
	{ name: "#", align: "r", getValue: p => Math.abs(p.quantity), sortDirection: -1 },
	{ name: "Expiry", align: "c", getValue: p => `${p.optionExpiry} (${p.dte})`, sortDirection: 1 },
	getAmount("Strike", p => p.optionStrike),
	getAmount("Price", p => p.underlyingPrice),
	{
		name: "ITM",
		align: "c",
		getValue: p => p.inTheMoney,
		render: r => r.value ? (<td key={r.key} className={r.column.align}><i className="bi bi-check" style={{ marginLeft: 6 }}></i></td>) : null
	},
	getAmount("Prem.", p => Math.abs(p.costBasis))
];

function Options(props) {
	const { portfolio, isLong, type } = props;
	const { positions } = portfolio;

	const optionPositions = positions
		.filter(p => p.option && p.optionType.startsWith(type.charAt(0)) && ((!isLong && p.quantity < 0) || (isLong && p.quantity > 0)))
		.sort((a, b) => {
			let value = a.dte - b.dte;
			if (value === 0) {
				value = a.symbol.localeCompare(b.symbol);
			}
			return value;
		});

	if (optionPositions.length === 0) {
		return null;
	}

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">{`${isLong ? "Long" : "Sold"} ${type}`}</div>
				<span style={{ marginLeft: 10 }}>({optionPositions.length})</span>
			</div>
			<DataTable columns={columns} records={optionPositions} />
		</div>
	);
}

export default Options;
