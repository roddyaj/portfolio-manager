import { useState } from "react";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

import DataTable from './DataTable';
import { renderPositionPopup } from './PositionPopup';

const columns = [
	{
		name: "Ticker",
		align: "l",
		getValue: p => p.symbol,
		render: r => (
			<td key={r.key} className={r.column.align}>
				<a href={`https://client.schwab.com/SymbolRouting.aspx?Symbol=${r.value}`} style={r.record.peRatio < 0 ? { backgroundColor: "#FDD" } : {}}>{r.value}</a>
			</td>
		)
	},
	{
		name: "",
		align: "l",
		getValue: p => p.symbol,
		render: r => (
			<td key={r.key} className={r.column.align}>
				<a href={`https://finance.yahoo.com/quote/${r.record.symbol}`}>
					<img src="https://s.yimg.com/cv/apiv2/default/icons/favicon_y19_32x32_custom.svg" alt="Yahoo" width={14} height={14} />
				</a>
			</td>
		)
	},
	{ name: "#", align: "r", getValue: p => Math.abs(p.quantity), modes: ["view"] },
	{ name: "Price", align: "r", getValue: p => p.price, render: r => r.value.toFixed(2) },
	{ name: "Value", align: "r", getValue: p => p.marketValue, render: r => r.value.toFixed(2), modes: ["view"] },
	{
		name: "Day",
		align: "r",
		getValue: p => p.dayChangePct,
		render: r => (<td key={r.key} className={r.column.align} style={{ color: r.value >= 0 ? "green" : "#C00" }}>{Math.abs(r.value).toFixed(2)}%</td>)
	},
	{
		name: "G/L",
		align: "r",
		getValue: p => p.gainLossPct,
		render: r => (<td key={r.key} className={r.column.align} style={{ color: r.value >= 0 ? "green" : "#C00" }}>{Math.abs(r.value).toFixed(2)}%</td>)
	},
	{ name: "Actual", align: "r", getValue: p => p.percentOfAccount, render: r => r.value ? r.value.toFixed(2) + "%" : "", modes: ["view"] },
	{ name: "Target", align: "r", getValue: p => p.targetPct, render: r => r.value ? r.value.toFixed(2) + "%" : "", modes: ["view"] },
	{ name: "Ratio", align: "r", getValue: p => p.percentOfAccount && p.targetPct ? (100 * p.percentOfAccount / p.targetPct) : null, render: r => r.value ? r.value.toFixed(1) + "%" : "", modes: ["view", "trades"] },
	{
		name: "Trade",
		align: "c",
		getValue: p => p.sharesToBuy * p.price,
		render: r => {
			const action = r.record.sharesToBuy > 0 ? "Buy" : "Sell";
			return (
				<td key={r.key} className={r.column.align}>
					<a href={`https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=${action}&Symbol=${r.record.symbol}`}>
						<button style={{ minWidth: 55 }} onClick={() => copyClip(Math.abs(r.record.sharesToBuy))}>{`${action} ${Math.abs(r.record.sharesToBuy)}`}</button>
					</a>
				</td>
			);
		},
		modes: ["trades"]
	},
	{
		name: "Open",
		align: "c",
		getValue: () => 0,
		render: r => {
			const action = r.record.sharesToBuy > 0 ? "Buy" : "Sell";
			const openBuyCount = r.record.openOrders ? r.record.openOrders.filter(o => o.action === "Buy").map(o => o.quantity).reduce((tot, cur) => tot + cur, 0) : 0;
			const openSellCount = r.record.openOrders ? r.record.openOrders.filter(o => o.action === "Sell").map(o => o.quantity).reduce((tot, cur) => tot + cur, 0) : 0;
			const openOrderArray = [['B', openBuyCount], ['S', openSellCount]].filter(a => a[1] !== 0);
			const openOrderText = r.tableState.mode === "view"
				? openOrderArray.map(a => a.join(" ")).join(", ")
				: openOrderArray.filter(a => a[0] === action.charAt(0)).map(a => a[1]).join("");
			return (
				<td key={r.key} className={r.column.align}>
					<a href="https://client.schwab.com/Trade/OrderStatus/ViewOrderStatus.aspx?ViewTypeFilter=Open">{openOrderText}</a>
				</td>
			)
		},
		modes: ["view", "trades"]
	},
	{
		name: "Amount",
		align: "r",
		getValue: p => p.sharesToBuy * p.price,
		render: r => r.value.toFixed(2),
		modes: ["trades"]
	},
	{
		name: "Sell Calls",
		align: "c",
		getValue: p => p.callsToSell,
		render: r => (<td key={r.key} className={r.column.align}><a href={`https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/${r.record.symbol}`}><button>Sell {r.value}</button></a></td>),
		modes: ["calls"]
	},
	{
		name: "",
		align: "l",
		getValue: () => 0,
		render: r => (
			<OverlayTrigger key={r.key} trigger={['hover', 'focus']} placement="right" overlay={renderPositionPopup(r.record)}>
				<td className={r.column.align}><i className="bi bi-info-circle" style={{ marginLeft: 6 }}></i></td>
			</OverlayTrigger>
		)
	}
];

function Positions2(props) {
	const { portfolio } = props;

	const [mode, setMode] = useState("view");

	let viewPositions = portfolio.positions.filter(p => !p.symbol.includes(" "));
	if (mode === "view") {
		viewPositions = viewPositions.sort((a, b) => b.marketValue - a.marketValue);
	} else if (mode === "trades") {
		viewPositions = viewPositions.filter(p => p.sharesToBuy).sort((a, b) => (a.sharesToBuy * a.price) - (b.sharesToBuy * b.price));
	} else if (mode === "calls") {
		viewPositions = viewPositions.filter(p => p.callsToSell).sort((a, b) => b.dayChangePct - a.dayChangePct);
	}

	const visibleColumns = columns.filter(c => !c.modes || c.modes.indexOf(mode) !== -1);

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">
					<input type="radio" name="positionMode" id="mode-view" value="view" checked={mode === "view"} onChange={(e) => setMode(e.target.value)} />
					<label htmlFor="mode-view" style={{ paddingLeft: 3, marginRight: 10 }}>Positions</label>
					<input type="radio" name="positionMode" id="mode-trade-shares" value="trades" checked={mode === "trades"} onChange={(e) => setMode(e.target.value)} />
					<label htmlFor="mode-trade-shares" style={{ paddingLeft: 3, marginRight: 10 }}>Trades</label>
					<input type="radio" name="positionMode" id="mode-sell-calls" value="calls" checked={mode === "calls"} onChange={(e) => setMode(e.target.value)} />
					<label htmlFor="mode-sell-calls" style={{ paddingLeft: 3, marginRight: 10 }}>Calls to Sell</label>
				</div>
				<span>({viewPositions.length})</span>
			</div>
			<DataTable columns={visibleColumns} records={viewPositions} tableState={{ mode }} />
		</div>
	);
}

function copyClip(text) {
	navigator.clipboard.writeText(text);
}

export default Positions2;
