import DataTable from './DataTable'

const columns = [
	{
		name: "Ticker",
		align: "l",
		getValue: p => p.symbol,
		render: r => (<td key={r.key} className={r.column.align}><a href={`https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/${r.value}`}>{r.value}</a></td>)
	},
	{
		name: "Price",
		align: "r",
		getValue: p => p.price,
		render: r => r.value ? r.value.toFixed(2) : null
	},
	{
		name: "Day",
		align: "r",
		getValue: p => p.dayChangePct,
		render: r => r.value ? (<td key={r.key} style={{ color: r.value >= 0 ? "green" : "#C00" }}>{Math.abs(r.value).toFixed(2)}%</td>) : null
	}
];

function PutsToSell(props) {
	const { portfolio } = props;

	const putsToSell = portfolio.putsToSell.sort((a, b) => a.dayChangePct - b.dayChangePct);

	if (putsToSell.length === 0) {
		return null;
	}

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">Puts To Sell</div>
				<span style={{ marginLeft: 10 }}>({putsToSell.length})</span>
			</div>
			<DataTable columns={columns} records={putsToSell} />
		</div>
	);
}

export default PutsToSell;
