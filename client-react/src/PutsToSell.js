import DataTable from './DataTable'
import { getAmount, getLink, getPctChange } from "./tableUtils";

const columns = [
	getLink("Ticker", p => p.symbol, r => `https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/${r.value}`),
	getAmount("Price", p => p.price),
	{ ...getPctChange("Day", p => p.dayChangePct), sortDirection: 1 },
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
